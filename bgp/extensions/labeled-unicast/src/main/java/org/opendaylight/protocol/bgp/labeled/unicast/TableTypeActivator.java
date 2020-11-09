/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.labeled.unicast;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.protocol.bgp.openconfig.spi.AbstractBGPTableTypeRegistryProviderActivator;
import org.opendaylight.protocol.bgp.openconfig.spi.BGPTableTypeRegistryProvider;
import org.opendaylight.protocol.bgp.openconfig.spi.BGPTableTypeRegistryProviderActivator;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev151009.IPV4LABELLEDUNICAST;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev151009.IPV6LABELLEDUNICAST;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.labeled.unicast.rev180329.LabeledUnicastSubsequentAddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev200120.Ipv4AddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev200120.Ipv6AddressFamily;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.osgi.service.component.annotations.Component;

@Singleton
@Component(immediate = true, service = BGPTableTypeRegistryProviderActivator.class,
           property = "type=org.opendaylight.protocol.bgp.labeled.unicast.TableTypeActivator")
@MetaInfServices(value = BGPTableTypeRegistryProviderActivator.class)
public final class TableTypeActivator extends AbstractBGPTableTypeRegistryProviderActivator {
    @Inject
    public TableTypeActivator() {
        // Exposed for DI
    }

    @Override
    protected List<AbstractRegistration> startBGPTableTypeRegistryProviderImpl(
            final BGPTableTypeRegistryProvider provider) {
        return List.of(
                provider.registerBGPTableType(Ipv4AddressFamily.class, LabeledUnicastSubsequentAddressFamily.class,
                    IPV4LABELLEDUNICAST.class),
                provider.registerBGPTableType(Ipv6AddressFamily.class, LabeledUnicastSubsequentAddressFamily.class,
                    IPV6LABELLEDUNICAST.class));
    }
}
