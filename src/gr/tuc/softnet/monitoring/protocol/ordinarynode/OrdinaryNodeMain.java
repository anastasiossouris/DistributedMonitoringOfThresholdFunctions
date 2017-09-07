

package gr.tuc.softnet.monitoring.protocol.ordinarynode;

import java.io.InputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import gr.tuc.softnet.monitoring.configuration.Configuration;
import gr.tuc.softnet.monitoring.protocol.coordinator.Coordinator;
import gr.tuc.softnet.monitoring.protocol.coordinator.OrdinaryNodeSession;
import gr.tuc.softnet.monitoring.protocol.ordinarynode.datastream.DataStream;
import gr.tuc.softnet.monitoring.protocol.ordinarynode.datastream.InputSource;
import gr.tuc.softnet.monitoring.protocol.ordinarynode.datastream.InputSourceReader;
import gr.tuc.softnet.monitoring.services.ServicesFactory;
import gr.tuc.softnet.monitoring.util.pair.Pair;

public class OrdinaryNodeMain {

	public static void main(String[] args) {
		
		final String coordinatorRmiRegistry = "coordinator.rmi.registry.host";
		final String coordinatorRmiRegistryPort = "coordinator.rmi.registry.port";
		final String coordinatorBoundName = "coordinator.rmi.registry.bind.name";
	
		// these must be given in the command line 
		final String xmlFilename = "properties.xml.filename";
		final String ordinaryNodeRmiRegistry = "ordinarynode.rmi.registry.host";
		final String ordinaryNodeRmiRegistryPort = "ordinarynode.rmi.registry.port";
		final String ordinaryNodeBoundName = "ordinarynode.rmi.registry.bind.name";
		final String ordinaryNodeName = "ordinarynode.name";
		
		try {
			ServicesFactory services = ServicesFactory.getInstance();
			
			/**
			 * First we need to load the configuration
			 */
			Configuration configuration = Configuration.getInsance();
			
			configuration.loadPropertiesFromXML(System.getProperty(xmlFilename));
			
			/**
			 * Now we must create a session with the coordinator
			 */
			OrdinaryNodeSession session = null;
			
			/**
			 *  find where the coordinator is
			 */
			Registry registry = LocateRegistry.getRegistry(
					System.getProperty(coordinatorRmiRegistry),
					Integer.valueOf(System.getProperty(coordinatorRmiRegistryPort))
					);
			Coordinator coordinator = (Coordinator)registry.lookup(System.getProperty(coordinatorBoundName));

			/**
			 *  export our remote object
			 */
			if (System.getSecurityManager() == null) {
	            System.setSecurityManager(new SecurityManager());
	        }

			OrdinaryNodeImpl ordinaryNode = new OrdinaryNodeImpl();
			OrdinaryNode stub = (OrdinaryNode)UnicastRemoteObject.exportObject(ordinaryNode, 0);
			Registry registry2 = LocateRegistry.getRegistry(System.getProperty(ordinaryNodeRmiRegistry), Integer.valueOf(System.getProperty(ordinaryNodeRmiRegistryPort)));
			registry2.rebind(System.getProperty(ordinaryNodeBoundName), stub);
			
			/**
			 *  ask the coordinator for a session
			 */
			session = coordinator.session(stub, System.getProperty(ordinaryNodeName));
			
			/**
			 * Start the data stream
			 */
			BlockingQueue<Pair<double [], Double>> queue = null;
			
			InputStream inputStream = services.getInputStream();
			InputSourceReader inputSourceReader = services.getInputSourceReader();
			
			inputSourceReader.setInputSource(new InputSource(inputStream));
			
			DataStream dataStream = new DataStream();
			dataStream.setInputSourceReader(inputSourceReader);
			dataStream.setBlockingQueue(queue);
			
			ExecutorService executorService = new ScheduledThreadPoolExecutor(1);
			
			executorService.submit(dataStream);
			
			
			/**
			 *  now set the values needed by the ordinary node
			 */
			ordinaryNode.setOrdinaryNodeSession(session);
			ordinaryNode.setMonitoredFunction(services.getMonitoredFunction());
			ordinaryNode.setDataStreamQueue(queue);		
			
			/**
			 * Now we must wait until the monitoring task is over...
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
