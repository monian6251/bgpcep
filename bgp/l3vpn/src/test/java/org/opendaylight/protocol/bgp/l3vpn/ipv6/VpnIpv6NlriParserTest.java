/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.l3vpn.ipv6;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.protocol.bgp.parser.BGPParsingException;
import org.opendaylight.protocol.util.ByteArray;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.labeled.unicast.rev150525.labeled.unicast.LabelStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.labeled.unicast.rev150525.labeled.unicast.LabelStackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.path.attributes.AttributesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.Attributes1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.Attributes1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.Attributes2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.Attributes2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.update.attributes.MpReachNlri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.update.attributes.MpReachNlriBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.update.attributes.MpUnreachNlri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.update.attributes.MpUnreachNlriBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.update.attributes.mp.reach.nlri.AdvertizedRoutesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev130919.update.attributes.mp.unreach.nlri.WithdrawnRoutesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.Ipv6AddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.RouteDistinguisher;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.RouteDistinguisherBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.vpn.ipv6.rev160331.l3vpn.ipv6.destination.VpnIpv6DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.vpn.rev160413.l3vpn.ip.destination.type.VpnDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.vpn.rev160413.l3vpn.ip.destination.type.VpnDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.concepts.rev131125.MplsLabel;

/**
 * @author Kevin Wang
 */
public class VpnIpv6NlriParserTest {

    private static final VpnIpv6NlriParser PARSER = new VpnIpv6NlriParser();

    private static final byte[] NLRI_TYPE1 = new byte[]{
        (byte) 0x88,    // length
        (byte) 0x00, (byte) 0x16, (byte) 0x31,  // label
        0, 1, 1, 2, 3, 4, 1, 2, // route distinguisher
        (byte) 0x20, (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x56, (byte) 0x89    // specify the IPv6 prefix here
    };
    private static final IpPrefix IPv6_PREFIX = new IpPrefix(new Ipv6Prefix("2001:2345:5689::/48"));
    private static final List<LabelStack> LABEL_STACK = Lists.newArrayList(
        new LabelStackBuilder().setLabelValue(new MplsLabel(355L)).build());
    private static final RouteDistinguisher DISTINGUISHER = RouteDistinguisherBuilder.getDefaultInstance("1.2.3.4:258");
    private static final VpnDestination VPN = new VpnDestinationBuilder().setRouteDistinguisher(DISTINGUISHER).setPrefix(IPv6_PREFIX).setLabelStack(LABEL_STACK).build();

    @Test
    public void testMpReachNlri() throws BGPParsingException {
        final MpReachNlriBuilder mpBuilder = new MpReachNlriBuilder();
        mpBuilder.setAfi(Ipv6AddressFamily.class);
        mpBuilder.setAdvertizedRoutes(new AdvertizedRoutesBuilder().setDestinationType(
            new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.vpn.ipv6.rev160331.update.attributes.mp.reach.nlri.advertized.routes.destination.type.DestinationVpnIpv6CaseBuilder().setVpnIpv6Destination(
                new VpnIpv6DestinationBuilder().setVpnDestination(Lists.newArrayList(VPN)).build()
            ).build()
            ).build()
        ).build();

        final MpReachNlri mpReachExpected = mpBuilder.build();

        final MpReachNlriBuilder testBuilder = new MpReachNlriBuilder();
        testBuilder.setAfi(Ipv6AddressFamily.class);
        PARSER.parseNlri(Unpooled.copiedBuffer(NLRI_TYPE1), testBuilder);
        Assert.assertEquals(mpReachExpected, testBuilder.build());

        final ByteBuf output = Unpooled.buffer();
        PARSER.serializeAttribute(
            new AttributesBuilder().addAugmentation(Attributes1.class,
                new Attributes1Builder().setMpReachNlri(mpReachExpected).build()
            ).build(), output
        );
        Assert.assertArrayEquals(NLRI_TYPE1, ByteArray.readAllBytes(output));
    }

    @Test
    public void testMpUnreachNlri() throws BGPParsingException {
        final MpUnreachNlriBuilder mpBuilder = new MpUnreachNlriBuilder();
        mpBuilder.setAfi(Ipv6AddressFamily.class);
        mpBuilder.setWithdrawnRoutes(
            new WithdrawnRoutesBuilder().setDestinationType(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.vpn.ipv6.rev160331.update.attributes.mp.unreach.nlri.withdrawn.routes.destination.type.DestinationVpnIpv6CaseBuilder().setVpnIpv6Destination(
                    new VpnIpv6DestinationBuilder().setVpnDestination(Lists.newArrayList(VPN)).build()
                ).build()
            ).build()
        ).build();

        final MpUnreachNlri mpUnreachExpected = mpBuilder.build();

        final MpUnreachNlriBuilder testBuilder = new MpUnreachNlriBuilder();
        testBuilder.setAfi(Ipv6AddressFamily.class);
        PARSER.parseNlri(Unpooled.copiedBuffer(NLRI_TYPE1), testBuilder);
        Assert.assertEquals(mpUnreachExpected, testBuilder.build());

        final ByteBuf output = Unpooled.buffer();
        PARSER.serializeAttribute(
            new AttributesBuilder().addAugmentation(Attributes2.class,
                new Attributes2Builder().setMpUnreachNlri(mpUnreachExpected).build()
            ).build(), output
        );
        Assert.assertArrayEquals(NLRI_TYPE1, ByteArray.readAllBytes(output));
    }
}
