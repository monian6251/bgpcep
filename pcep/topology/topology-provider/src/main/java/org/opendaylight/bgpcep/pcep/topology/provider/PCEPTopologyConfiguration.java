/*
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bgpcep.pcep.topology.provider;

import static java.util.Objects.requireNonNull;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.protocol.concepts.KeyMapping;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.graph.rev191125.graph.topology.GraphKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.config.rev181109.PcepNodeConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.config.rev181109.PcepTopologyTypeConfig;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.concepts.Immutable;

final class PCEPTopologyConfiguration implements Immutable {
    private final @NonNull InetSocketAddress address;
    private final @NonNull KeyMapping keys;
    private final short rpcTimeout;
    private final GraphKey graphKey;

    PCEPTopologyConfiguration(final @NonNull InetSocketAddress address, final short rpcTimeout,
            final @NonNull KeyMapping keys, final @NonNull GraphKey graphKey) {
        this.address = requireNonNull(address);
        this.keys = requireNonNull(keys);
        this.rpcTimeout = rpcTimeout;
        this.graphKey = requireNonNull(graphKey);
    }

    static @Nullable PCEPTopologyConfiguration of(final @NonNull Topology topology) {
        // FIXME: this should live in the pcep topology type's presence container and be mandatory
        final var pcepConfig = topology.augmentation(PcepTopologyTypeConfig.class);
        if (pcepConfig == null) {
            return null;
        }
        final var sessionConfig = pcepConfig.getSessionConfig();
        if (sessionConfig == null) {
            return null;
        }

        return new PCEPTopologyConfiguration(
            getInetSocketAddress(sessionConfig.getListenAddress(), sessionConfig.getListenPort()),
            sessionConfig.getRpcTimeout(), constructKeys(topology.getNode()),
            constructGraphKey(sessionConfig.getTedName()));
    }

    short getRpcTimeout() {
        return rpcTimeout;
    }

    @NonNull InetSocketAddress getAddress() {
        return address;
    }

    @NonNull KeyMapping getKeys() {
        return keys;
    }

    @NonNull GraphKey getGraphKey() {
        return graphKey;
    }

    private static @NonNull KeyMapping constructKeys(final @Nullable Map<NodeKey, Node> nodes) {
        if (nodes == null) {
            return KeyMapping.of();
        }

        final var passwords = new HashMap<InetAddress, String>();
        for (var node : nodes.values()) {
            if (node != null) {
                final var nodeConfig = node.augmentation(PcepNodeConfig.class);
                if (nodeConfig != null) {
                    final var sessionConfig = nodeConfig.getSessionConfig();
                    if (sessionConfig != null) {
                        final var rfc2385KeyPassword = sessionConfig.getPassword();
                        if (rfc2385KeyPassword != null) {
                            final String password = rfc2385KeyPassword.getValue();
                            if (!password.isEmpty()) {
                                passwords.put(nodeAddress(node), password);
                            }
                        }
                    }
                }
            }
        }

        return KeyMapping.of(passwords);
    }

    private static @NonNull GraphKey constructGraphKey(String name) {
        return new GraphKey(name.startsWith("ted://") ? name : "ted://" + name);
    }

    private static InetAddress nodeAddress(final Node node) {
        return InetAddresses.forString(node.getNodeId().getValue());
    }

    private static @NonNull InetSocketAddress getInetSocketAddress(final IpAddressNoZone address,
            final PortNumber port) {
        return new InetSocketAddress(IetfInetUtil.INSTANCE.inetAddressForNoZone(requireNonNull(address)),
            port.getValue().toJava());
    }
}
