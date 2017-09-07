package gr.tuc.softnet.monitoring.configuration;

import gr.tuc.softnet.monitoring.services.ServicesFactory;

/**
 * {@link Properties} contains constants that are used by the {@link ServicesFactory} and to obtain at runtime various information.
 * 
 * @author Tassos Souris
 */
public final class Properties {
	public static final String MONITORED_FUNCTION = "monitoredfunction.class.name";
	public static final String COORDINATOR_RMI_REGISTRY_HOST = "coordinator.rmi.registry.host";
	public static final String COORDINATOR_RMI_REGISTRY_PORT = "coordinator.rmi.registry.port";
	public static final String COORDINATOR_RMI_BIND_NAME = "coordinator.rmi.registry.bind.name";
	public static final String PROPERTIES_XML_FILENAME = "properties.xml.filename";
	public static final String ORDINARY_NODE_NAMES_FILENAME = "ordinarynode.names.filename";
}
