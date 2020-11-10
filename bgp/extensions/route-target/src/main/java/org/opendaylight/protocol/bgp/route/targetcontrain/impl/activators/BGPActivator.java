/*
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.route.targetcontrain.impl.activators;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.protocol.bgp.inet.codec.nexthop.Ipv4NextHopParserSerializer;
import org.opendaylight.protocol.bgp.parser.spi.AbstractBGPExtensionProviderActivator;
import org.opendaylight.protocol.bgp.parser.spi.BGPExtensionProviderActivator;
import org.opendaylight.protocol.bgp.parser.spi.BGPExtensionProviderContext;
import org.opendaylight.protocol.bgp.route.targetcontrain.impl.nlri.RouteTargetConstrainNlriHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.route.target.constrain.rev180618.RouteTargetConstrainSubsequentAddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.route.target.constrain.rev180618.route.target.constrain.routes.RouteTargetConstrainRoutes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev200120.Ipv4AddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev200120.next.hop.c.next.hop.Ipv4NextHopCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev200120.next.hop.c.next.hop.Ipv6NextHopCase;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Component;

/**
 * Registers NLRI, Attributes, Extended communities Handlers.
 *
 * @author Claudio D. Gasparini
 */
@Singleton
@Component(immediate = true, service = BGPExtensionProviderActivator.class,
           property = "type=org.opendaylight.protocol.bgp.route.targetcontrain.impl.activators.BGPActivator")
@MetaInfServices(value = BGPExtensionProviderActivator.class)
public final class BGPActivator extends AbstractBGPExtensionProviderActivator {
    @VisibleForTesting
    static final int RT_SAFI = 132;

    @Inject
    public BGPActivator() {
        // Exposed for DI
    }

    @Override
    protected List<Registration> startImpl(final BGPExtensionProviderContext context) {
        final RouteTargetConstrainNlriHandler routeTargetNlriHandler = new RouteTargetConstrainNlriHandler();
        return List.of(
            context.registerSubsequentAddressFamily(RouteTargetConstrainSubsequentAddressFamily.class, RT_SAFI),
            context.registerNlriParser(Ipv4AddressFamily.class, RouteTargetConstrainSubsequentAddressFamily.class,
                routeTargetNlriHandler, new Ipv4NextHopParserSerializer(), Ipv4NextHopCase.class,
                Ipv6NextHopCase.class),
            context.registerNlriSerializer(RouteTargetConstrainRoutes.class, routeTargetNlriHandler));
    }
}
