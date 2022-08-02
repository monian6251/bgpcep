/*
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bgpcep.pcep.topology.provider;

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.stats.rev171113.PcepSessionState;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Topology Node Sessions stats handler. Will store Session stats on DS per each Topology Node registered.
 */
interface TopologySessionStatsRegistry {
    /**
     * Register session to Session stats Registry handler.
     *
     * @param nodeId       Identifier of the topology node where it will be stored session stats under DS
     * @param sessionState containing all Stats Session information
     */
    // FIXME: BGPCEP-1105: nodeId is a bit superfluous, lifecycle is driven by AbstractTopologySessionListener
    @CheckReturnValue
    <T extends PcepSessionState> @NonNull ObjectRegistration<T> bind(
        @NonNull KeyedInstanceIdentifier<Node, NodeKey> nodeId, @NonNull T sessionState);
}
