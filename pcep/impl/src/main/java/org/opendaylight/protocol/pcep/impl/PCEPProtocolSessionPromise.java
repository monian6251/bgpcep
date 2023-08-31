/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.opendaylight.protocol.pcep.PCEPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class is thread-safe
public final class PCEPProtocolSessionPromise<S extends PCEPSession> extends DefaultPromise<S> {
    private static final Logger LOG = LoggerFactory.getLogger(PCEPProtocolSessionPromise.class);
    private InetSocketAddress address;
    private final int retryTimer;
    private final int connectTimeout;
    private final Bootstrap bootstrap;
    @GuardedBy("this")
    private Future<?> pending;

    PCEPProtocolSessionPromise(final EventExecutor executor, final InetSocketAddress address,
            final int retryTimer, final int connectTimeout, final Bootstrap bootstrap) {
        super(executor);
        this.address = requireNonNull(address);
        this.retryTimer = retryTimer;
        this.connectTimeout = connectTimeout;
        this.bootstrap = requireNonNull(bootstrap);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    synchronized void connect() {
        final PCEPProtocolSessionPromise<?> lock = this;

        try {
            LOG.debug("Promise {} attempting connect for {}ms", lock, connectTimeout);
            if (address.isUnresolved()) {
                address = new InetSocketAddress(address.getHostName(), address.getPort());
            }

            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
            bootstrap.remoteAddress(address);
            final ChannelFuture connectFuture = bootstrap.connect();
            connectFuture.addListener(new BootstrapConnectListener());
            pending = connectFuture;
        } catch (RuntimeException e) {
            LOG.info("Failed to connect to {}", address, e);
            setFailure(e);
        }
    }

    @Override
    public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        if (super.cancel(mayInterruptIfRunning)) {
            pending.cancel(mayInterruptIfRunning);
            return true;
        }

        return false;
    }

    @Override
    public synchronized Promise<S> setSuccess(final S result) {
        LOG.debug("Promise {} completed", this);
        return super.setSuccess(result);
    }

    private final class BootstrapConnectListener implements ChannelFutureListener {
        @Override
        public void operationComplete(final ChannelFuture cf) {
            synchronized (PCEPProtocolSessionPromise.this) {
                PCEPProtocolSessionPromise.LOG.debug("Promise {} connection resolved",
                        PCEPProtocolSessionPromise.this);
                Preconditions.checkState(PCEPProtocolSessionPromise.this.pending.equals(cf));
                if (isCancelled()) {
                    if (cf.isSuccess()) {
                        PCEPProtocolSessionPromise.LOG.debug("Closing channel for cancelled promise {}",
                                PCEPProtocolSessionPromise.this);
                        cf.channel().close();
                    }
                } else if (cf.isSuccess()) {
                    PCEPProtocolSessionPromise.LOG.debug("Promise {} connection successful",
                            PCEPProtocolSessionPromise.this);
                } else {
                    PCEPProtocolSessionPromise.LOG.debug("Attempt to connect to {} failed",
                            PCEPProtocolSessionPromise.this.address, cf.cause());

                    if (PCEPProtocolSessionPromise.this.retryTimer == 0) {
                        PCEPProtocolSessionPromise.LOG
                                .debug("Retry timer value is 0. Reconnection will not be attempted");
                        setFailure(cf.cause());
                        return;
                    }

                    final EventLoop loop = cf.channel().eventLoop();
                    loop.schedule(() -> {
                        synchronized (PCEPProtocolSessionPromise.this) {
                            PCEPProtocolSessionPromise.LOG.debug("Attempting to connect to {}",
                                    PCEPProtocolSessionPromise.this.address);
                            final Future<Void> reconnectFuture = PCEPProtocolSessionPromise.this.bootstrap.connect();
                            reconnectFuture.addListener(BootstrapConnectListener.this);
                            PCEPProtocolSessionPromise.this.pending = reconnectFuture;
                        }
                    }, PCEPProtocolSessionPromise.this.retryTimer, TimeUnit.SECONDS);
                    PCEPProtocolSessionPromise.LOG.debug("Next reconnection attempt in {}s",
                            PCEPProtocolSessionPromise.this.retryTimer);
                }
            }
        }
    }
}
