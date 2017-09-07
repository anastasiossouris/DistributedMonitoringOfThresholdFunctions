package gr.tuc.softnet.monitoring.protocol.coordinator;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.List;

import gr.tuc.softnet.monitoring.configuration.Configuration;
import gr.tuc.softnet.monitoring.configuration.Properties;
import gr.tuc.softnet.monitoring.services.ServicesFactory;

public class CoordinatorMain {

	public static void main(String[] args) {
		try{
			// First we need to load the configuration
			Configuration configuration = Configuration.getInsance();
			
			configuration.loadPropertiesFromXML(System.getProperty(Properties.PROPERTIES_XML_FILENAME));
			
			List<String> nodeNames = configuration.loadOrdinaryNodeNames(System.getProperty(Properties.ORDINARY_NODE_NAMES_FILENAME));
			
			// Create the remote object implementation
			CoordinatorImpl coordinatorImpl = new CoordinatorImpl();
			
			coordinatorImpl.setMonitoredFunction(ServicesFactory.getInstance().getMonitoredFunction());
			coordinatorImpl.setOrdinayNodeNames(new HashSet<String>(nodeNames));
			
			// export the remote object to rmi
			 if (System.getSecurityManager() == null) {
		         System.setSecurityManager(new SecurityManager());
		     }
			 
			 String coordinatorBindName = System.getProperty(Properties.COORDINATOR_RMI_BIND_NAME);
			 String coordinatorRmiHost = System.getProperty(Properties.COORDINATOR_RMI_REGISTRY_HOST);
			 int coordinatorRmiPort = Integer.valueOf(System.getProperty(Properties.COORDINATOR_RMI_REGISTRY_PORT));
			 
			 Coordinator stub = (Coordinator)UnicastRemoteObject.exportObject(coordinatorImpl, 0);
			 Registry registry = LocateRegistry.getRegistry(coordinatorRmiHost,coordinatorRmiPort);
	         registry.rebind(coordinatorBindName, stub);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
