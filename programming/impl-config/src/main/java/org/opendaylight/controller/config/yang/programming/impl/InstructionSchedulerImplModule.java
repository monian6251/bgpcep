/**
 * Generated file

 * Generated from: yang module name: config-programming-impl  yang module local name: instruction-scheduler-impl
 * Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
 * Generated at: Mon Nov 18 16:50:17 CET 2013
 *
 * Do not modify this file unless it is present under src/main directory
 */
package org.opendaylight.controller.config.yang.programming.impl;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opendaylight.bgpcep.programming.impl.ProgrammingServiceImpl;
import org.opendaylight.bgpcep.programming.spi.InstructionExecutor;
import org.opendaylight.bgpcep.programming.spi.InstructionScheduler;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.programming.rev130930.ProgrammingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.programming.rev130930.SubmitInstructionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.programming.rev130930.submit.instruction.output.result.failure._case.Failure;

/** 
 *
 */
public final class InstructionSchedulerImplModule extends
		org.opendaylight.controller.config.yang.programming.impl.AbstractInstructionSchedulerImplModule {

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
	public void validate() {
		super.validate();
		// Add custom validation for module attributes here.
	}

	@Override
	public java.lang.AutoCloseable createInstance() {
		// FIXME: BUG-192 : configured timer
		final Timer timer = new HashedWheelTimer();

		final ExecutorService exec = Executors.newSingleThreadExecutor();

		final ProgrammingServiceImpl inst = new ProgrammingServiceImpl(getNotificationServiceDependency(), exec, timer);

		final RpcRegistration<ProgrammingService> reg = getRpcRegistryDependency().addRpcImplementation(ProgrammingService.class, inst);

		final class ProgrammingServiceImplCloseable implements InstructionScheduler, AutoCloseable {
			@Override
			public void close() throws Exception {
				try {
					reg.close();
				} finally {
					try {
						inst.close();
					} finally {
						exec.shutdown();
					}
				}
			}

			@Override
			public Failure submitInstruction(final SubmitInstructionInput input, final InstructionExecutor executor) {
				return inst.submitInstruction(input, executor);
			}
		}

		return new ProgrammingServiceImplCloseable();
	}
}
