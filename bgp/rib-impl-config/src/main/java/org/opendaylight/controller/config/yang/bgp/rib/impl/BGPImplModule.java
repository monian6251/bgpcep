/**
 * Generated file

 * Generated from: yang module name: bgp-rib-impl  yang module local name: bgp-impl
 * Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
 * Generated at: Wed Nov 06 13:02:31 CET 2013
 *
 * Do not modify this file unless it is present under src/main directory
 */
package org.opendaylight.controller.config.yang.bgp.rib.impl;

import java.net.InetSocketAddress;

import org.opendaylight.controller.config.api.JmxAttributeValidationException;
import org.opendaylight.protocol.bgp.rib.impl.BGPImpl;

/**
*
*/
public final class BGPImplModule
		extends
		org.opendaylight.controller.config.yang.bgp.rib.impl.AbstractBGPImplModule {

	public BGPImplModule(
			org.opendaylight.controller.config.api.ModuleIdentifier name,
			org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
		super(name, dependencyResolver);
	}

	public BGPImplModule(
			org.opendaylight.controller.config.api.ModuleIdentifier name,
			org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
			BGPImplModule oldModule, java.lang.AutoCloseable oldInstance) {
		super(name, dependencyResolver, oldModule, oldInstance);
	}

	@Override
	public void validate() {
		super.validate();
		JmxAttributeValidationException.checkNotNull(getHost(),
				"value is not set.", hostJmxAttribute);

		JmxAttributeValidationException.checkNotNull(getPort(),
				"value is not set.", portJmxAttribute);
		JmxAttributeValidationException.checkCondition((getPort() >= 0)
				&& (getPort() <= 65535), "value" + getPort()
				+ " is out of range (0-65535).", portJmxAttribute);
	}

	@Override
	public java.lang.AutoCloseable createInstance() {
		InetSocketAddress address = new InetSocketAddress(getHost(), getPort());
		return new BGPImpl(getBgpDispatcherDependency(), address,
				getBgpProposalDependency());
	}
}
