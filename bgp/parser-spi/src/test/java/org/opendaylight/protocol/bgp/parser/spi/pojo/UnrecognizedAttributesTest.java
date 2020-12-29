/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.parser.spi.pojo;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import io.netty.buffer.Unpooled;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.protocol.bgp.parser.BGPDocumentedException;
import org.opendaylight.protocol.bgp.parser.BGPParsingException;
import org.opendaylight.protocol.util.ByteArray;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev200120.path.attributes.attributes.UnrecognizedAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev200120.path.attributes.attributes.UnrecognizedAttributesKey;

public class UnrecognizedAttributesTest {

    private static final int UNRECOGNIZED_ATTRIBUTE_COUNT = 1;
    private static final int FIRST_ATTRIBUTE = 0;
    private static final short NON_EXISTENT_TYPE = 0;
    private static final int NON_VALUE_BYTES = 3;

    private static final SimpleAttributeRegistry SIMPLE_ATTR_REG = new SimpleAttributeRegistry();

    @Test
    public void testUnrecognizedAttributesWithoutOptionalFlag() throws BGPDocumentedException, BGPParsingException {
        final BGPDocumentedException ex = assertThrows(BGPDocumentedException.class, () -> {
            SIMPLE_ATTR_REG.parseAttributes(
                Unpooled.wrappedBuffer(new byte[] { 0x03, 0x00, 0x05, 0x01, 0x02, 0x03, 0x04, 0x05 }), null);
        });

        assertEquals("Well known attribute not recognized.", ex.getMessage());
    }

    @Test
    public void testUnrecognizedAttributes() throws BGPDocumentedException, BGPParsingException {
        final byte[] attributeBytes = { (byte)0xe0, 0x00, 0x05, 0x01, 0x02, 0x03, 0x04, 0x05 };
        final Map<UnrecognizedAttributesKey, UnrecognizedAttributes> unrecogAttribs = SIMPLE_ATTR_REG.parseAttributes(
            Unpooled.wrappedBuffer(attributeBytes), null).getAttributes().getUnrecognizedAttributes();
        assertEquals(UNRECOGNIZED_ATTRIBUTE_COUNT, unrecogAttribs.size());
        final UnrecognizedAttributes unrecogAttrib = unrecogAttribs.values().iterator().next();
        final UnrecognizedAttributesKey expectedAttribKey =
            new UnrecognizedAttributesKey(unrecogAttrib.getType());

        assertTrue(unrecogAttrib.getPartial());
        assertTrue(unrecogAttrib.getTransitive());
        assertArrayEquals(ByteArray.cutBytes(attributeBytes, NON_VALUE_BYTES), unrecogAttrib.getValue());
        assertEquals(NON_EXISTENT_TYPE, unrecogAttrib.getType().shortValue());
        assertEquals(expectedAttribKey, unrecogAttrib.key());
    }
}
