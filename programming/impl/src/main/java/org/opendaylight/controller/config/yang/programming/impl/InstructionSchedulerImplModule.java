/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Generated file

 * Generated from: yang module name: config-programming-impl  yang module local name: instruction-scheduler-impl
 * Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
 * Generated at: Mon Nov 18 16:50:17 CET 2013
 *
 * Do not modify this file unless it is present under src/main directory
 */
package org.opendaylight.controller.config.yang.programming.impl;

import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;
import java.lang.reflect.Method;
import org.opendaylight.bgpcep.programming.impl.IntructionDeployer;
import org.opendaylight.bgpcep.programming.spi.InstructionScheduler;
import org.opendaylight.controller.config.api.osgi.WaitingServiceTracker;
import org.osgi.framework.BundleContext;

/**
 * @deprecated Replaced by blueprint wiring
 */
public final class InstructionSchedulerImplModule extends
    org.opendaylight.controller.config.yang.programming.impl.AbstractInstructionSchedulerImplModule {

    private BundleContext bundleContext;

    public InstructionSchedulerImplModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier,
        final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public InstructionSchedulerImplModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier,
        final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
        final InstructionSchedulerImplModule oldModule, final java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    protected void customValidation() {
        // Add custom validation for module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final WaitingServiceTracker<IntructionDeployer> intructionDeployerTracker =
            WaitingServiceTracker.create(IntructionDeployer.class, this.bundleContext);
        final IntructionDeployer intructionDeployer = intructionDeployerTracker
            .waitForService(WaitingServiceTracker.FIVE_MINUTES);

        final String instructionId = getInstructionQueueId() != null ? getInstructionQueueId() :
            getIdentifier().getInstanceName();
        intructionDeployer.createInstruction(instructionId);
        final WaitingServiceTracker<InstructionScheduler> instructionSchedulerTracker = WaitingServiceTracker
            .create(InstructionScheduler.class,
            this.bundleContext, "(" + InstructionScheduler.class.getName() + "=" + instructionId + ")");
        final InstructionScheduler instructionScheduler = instructionSchedulerTracker
            .waitForService(WaitingServiceTracker.FIVE_MINUTES);

        return Reflection.newProxy(ProgrammingServiceImplCloseable.class, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(final Object proxy, final Method method, final Object[] args) throws Throwable {
                if (method.getName().equals("close")) {
                    intructionDeployer.removeInstruction(instructionId);
                    intructionDeployerTracker.close();
                    return null;
                } else {
                    return method.invoke(instructionScheduler, args);
                }
            }
        });
    }

    void setBundleContext(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private interface ProgrammingServiceImplCloseable extends InstructionScheduler, AutoCloseable {
    }
}
