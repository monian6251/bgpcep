/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;
import org.opendaylight.protocol.pcep.PCEPSessionListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.pcep.app.config.rev160707.pcep.dispatcher.config.Tls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.open.object.Open;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.open.object.OpenBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public final class DefaultPCEPSessionNegotiator extends AbstractPCEPSessionNegotiator {
    private final PCEPSessionListener listener;
    private final int maxUnknownMessages;

    public DefaultPCEPSessionNegotiator(final Promise<PCEPSessionImpl> promise, final Channel channel,
            final PCEPSessionListener listener, final Uint8 sessionId, final int maxUnknownMessages,
            final Open localPrefs, final Tls tlsConfiguration) {
        super(promise, channel, tlsConfiguration);
        this.listener = requireNonNull(listener);
        this.maxUnknownMessages = maxUnknownMessages;
        myLocalPrefs = new OpenBuilder()
                .setKeepalive(localPrefs.getKeepalive())
                .setDeadTimer(localPrefs.getDeadTimer())
                .setSessionId(requireNonNull(sessionId))
                .setTlvs(localPrefs.getTlvs())
                .build();
    }

    public DefaultPCEPSessionNegotiator(final Promise<PCEPSessionImpl> promise, final Channel channel,
            final PCEPSessionListener listener, final Uint8 sessionId, final int maxUnknownMessages,
            final Open localPrefs) {
        this(promise, channel, listener, sessionId, maxUnknownMessages, localPrefs, null);
    }

    private final Open myLocalPrefs;

    @Override
    protected Open getInitialProposal() {
        return myLocalPrefs;
    }

    @Override
    @VisibleForTesting
    public PCEPSessionImpl createSession(final Channel channel, final Open localPrefs, final Open remotePrefs) {
        return new PCEPSessionImpl(listener, maxUnknownMessages, channel, localPrefs, remotePrefs);
    }

    @Override
    protected boolean isProposalAcceptable(final Open open) {
        return true;
    }

    @Override
    protected Open getCounterProposal(final Open open) {
        return null;
    }

    @Override
    protected Open getRevisedProposal(final Open suggestion) {
        return myLocalPrefs;
    }
}
