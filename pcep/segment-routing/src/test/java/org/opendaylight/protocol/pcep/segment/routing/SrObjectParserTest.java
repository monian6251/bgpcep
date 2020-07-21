/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.segment.routing;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.protocol.pcep.parser.object.PCEPExplicitRouteObjectParser;
import org.opendaylight.protocol.pcep.spi.ObjectHeaderImpl;
import org.opendaylight.protocol.pcep.spi.PCEPDeserializerException;
import org.opendaylight.protocol.pcep.spi.TlvRegistry;
import org.opendaylight.protocol.pcep.spi.VendorInformationTlvRegistry;
import org.opendaylight.protocol.pcep.spi.pojo.SimplePCEPExtensionProviderContext;
import org.opendaylight.protocol.util.ByteArray;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.pcep.sync.optimizations.rev200720.Tlvs3Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.segment.routing.rev200720.NaiType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.segment.routing.rev200720.Tlvs1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.segment.routing.rev200720.add.lsp.input.arguments.ero.subobject.subobject.type.SrEroTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.segment.routing.rev200720.sr.pce.capability.tlv.SrPceCapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.segment.routing.rev200720.sr.subobject.nai.IpNodeIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.ProtocolVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.explicit.route.object.EroBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.explicit.route.object.ero.Subobject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.explicit.route.object.ero.SubobjectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.open.object.OpenBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.open.object.open.TlvsBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public class SrObjectParserTest {

    private static final byte[] OPEN_OBJECT_BYTES = {
        0x01,0x10,0x00,0x10,
        0x20,0x1e,0x78,0x01,
        /* sr-capability-tlv */
        0x00,0x1a,0x00,0x04,
        0x00,0x00,0x03,0x01};

    private static final byte[] SR_ERO_OBJECT_BYTES = {
        0x07,0x10,0x00,0x10,
        /* ero-subobject */
        0x24,0x0c,(byte) 0x10,0x00,
        0x00,0x01,(byte)0xe2,0x40,
        0x4A,0x7D,0x2b,0x63,
    };

    private TlvRegistry tlvRegistry;
    private VendorInformationTlvRegistry viTlvRegistry;

    private SimplePCEPExtensionProviderContext ctx;
    private SegmentRoutingActivator act;

    @Before
    public void setUp() {
        this.ctx = new SimplePCEPExtensionProviderContext();
        this.act = new SegmentRoutingActivator();
        this.act.start(this.ctx);
        this.tlvRegistry = this.ctx.getTlvHandlerRegistry();
        this.viTlvRegistry = this.ctx.getVendorInformationTlvRegistry();
    }

    @Test
    public void testOpenObjectWithSpcTlv() throws PCEPDeserializerException {
        final PcepOpenObjectWithSpcTlvParser parser = new PcepOpenObjectWithSpcTlvParser(this.tlvRegistry,
            this.viTlvRegistry);

        final OpenBuilder builder = new OpenBuilder()
                .setProcessingRule(false)
                .setIgnore(false)
                .setVersion(new ProtocolVersion(Uint8.ONE))
                .setKeepalive(Uint8.valueOf(30))
                .setDeadTimer(Uint8.valueOf(120))
                .setSessionId(Uint8.ONE);

        builder.setTlvs(new TlvsBuilder()
                .addAugmentation(new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful
                    .rev200720.Tlvs1Builder().build())
                .addAugmentation(new Tlvs1Builder()
                    .setSrPceCapability(new SrPceCapabilityBuilder().setNFlag(Boolean.TRUE).setXFlag(Boolean.TRUE)
                        .setMsd(Uint8.ONE).build())
                    .build())
                .addAugmentation(new Tlvs3Builder().build()).build());

        final ByteBuf result = Unpooled.wrappedBuffer(OPEN_OBJECT_BYTES);
        assertEquals(builder.build(),
                parser.parseObject(new ObjectHeaderImpl(false, false), result.slice(4, result.readableBytes() - 4)));
        final ByteBuf buffer = Unpooled.buffer();
        parser.serializeObject(builder.build(), buffer);
        parser.serializeTlvs(null, Unpooled.EMPTY_BUFFER);
        parser.serializeTlvs(new TlvsBuilder().build(), Unpooled.EMPTY_BUFFER);
        assertArrayEquals(OPEN_OBJECT_BYTES, ByteArray.getAllBytes(buffer));
    }

    @Test
    public void testSrEroObjectWithSubobjects() throws PCEPDeserializerException {
        final PCEPExplicitRouteObjectParser parser = new PCEPExplicitRouteObjectParser(
            this.ctx.getEROSubobjectHandlerRegistry());

        final EroBuilder builder = new EroBuilder();
        builder.setProcessingRule(false);
        builder.setIgnore(false);
        final List<Subobject> subobjects = new ArrayList<>();

        final SrEroTypeBuilder srEroSubBuilder = new SrEroTypeBuilder()
                .setCFlag(false)
                .setMFlag(false)
                .setNaiType(NaiType.Ipv4NodeId)
                .setSid(Uint32.valueOf(123456))
                .setNai(new IpNodeIdBuilder().setIpAddress(new IpAddressNoZone(
                    new Ipv4AddressNoZone("74.125.43.99"))).build());
        final SubobjectBuilder subobjBuilder = new SubobjectBuilder().setSubobjectType(srEroSubBuilder.build())
                .setLoose(false);
        subobjects.add(subobjBuilder.build());

        builder.setSubobject(subobjects);

        final ByteBuf result = Unpooled.wrappedBuffer(SR_ERO_OBJECT_BYTES);
        assertEquals(builder.build(),
                parser.parseObject(new ObjectHeaderImpl(false, false), result.slice(4, result.readableBytes() - 4)));
        final ByteBuf buffer = Unpooled.buffer();
        parser.serializeObject(builder.build(), buffer);
        assertArrayEquals(SR_ERO_OBJECT_BYTES, ByteArray.getAllBytes(buffer));
    }

    @Test
    public void testSrEroSerializerWithUpdateLspAugmentation() throws PCEPDeserializerException {
        final PCEPExplicitRouteObjectParser parser = new PCEPExplicitRouteObjectParser(
            this.ctx.getEROSubobjectHandlerRegistry());

        final EroBuilder builder = new EroBuilder();
        builder.setProcessingRule(false);
        builder.setIgnore(false);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.segment.routing.rev200720.update.lsp
            .input.arguments.ero.subobject.subobject.type.SrEroTypeBuilder srEroSubBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.segment.routing.rev200720
                    .update.lsp.input.arguments.ero.subobject.subobject.type.SrEroTypeBuilder()
                    .setCFlag(false)
                    .setMFlag(false)
                    .setNaiType(NaiType.Ipv4NodeId)
                    .setSid(Uint32.valueOf(123456))
                    .setNai(new IpNodeIdBuilder().setIpAddress(new IpAddressNoZone(
                        new Ipv4AddressNoZone("74.125.43.99"))).build());
        final SubobjectBuilder subobjBuilder = new SubobjectBuilder().setSubobjectType(srEroSubBuilder.build())
                .setLoose(false);
        builder.setSubobject(Lists.newArrayList(subobjBuilder.build()));

        final ByteBuf buffer = Unpooled.buffer();
        parser.serializeObject(builder.build(), buffer);
        assertArrayEquals(SR_ERO_OBJECT_BYTES, ByteArray.getAllBytes(buffer));
    }
}
