package gr.tuc.softnet.monitoring.configuration;

import gr.tuc.softnet.monitoring.protocol.coordinator.Coordinator;
import gr.tuc.softnet.monitoring.protocol.ordinarynode.OrdinaryNode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * {@link Configuration} is a singleton object used to load the necessary configuration needed for the coordinator and the ordinary nodes.
 * 
 * @author Tassos Souris
 */
public class Configuration {
	private static Configuration instance = null; // the singleton instance
	
	private Configuration(){
		
	}
	
	/**
	 * @return The {@link Configuration} singleton object.
	 */
	public static synchronized Configuration getInsance(){
		if (instance == null){
			instance = new Configuration();
		}
		return instance;
	}
	
	/**
	 * Used to load the properties needed for the configuration from an XML file.
	 * 
	 * @throws IOException 
	 */
	public void loadPropertiesFromXML(String filename) throws IOException{
		Properties properties = new Properties(); // the properties object to load the properties into
		BufferedInputStream stream = null; // the stream to open the xml file
		
		try{
			// Open the stream from the xml file
			stream = new BufferedInputStream(new FileInputStream(filename));
			
			// Load the properties
			properties.loadFromXML(stream);
			
			// Save the properties in the System properties
			System.setProperties(properties);
		}
		finally{
			if (stream != null){
				stream.close();
			}
		}
		
	}
	
	/**
	 * Load from a file whose name is given as parameter the names of the {@link OrdinaryNode} objects expected to establish a session with the {@link Coordinator}.
	 * 
	 * The format of the file is of the form:
	 * name1[newline]
	 * name2[newline]
	 * ...
	 * nameN[newline]
	 * 
	 * @return A {@link List} object with the names of the {@link OrdinaryNode} objects.
	 * @throws IOException 
	 */
	public List<String> loadOrdinaryNodeNames(String filename) throws IOException{
		List<String> names = new LinkedList<String>();
		BufferedReader reader = null;
		String line = null;
		
		try{
			// Open the file to read
			reader = new BufferedReader(new FileReader(filename));
			
			// Read the lines from the file and store them in the names list
			while ( (line = reader.readLine()) != null ){
				names.add(line);
			}
		}
		finally{
			if (reader != null){
				reader.close();
			}
		}
		
		return names;
	}
	
	/**
	 * Load from a file whose name is given as parameter the names of the {@link OrdinaryNode} objects.
	 * 
	 * The format of the file is of the form:
	 * name1[newline]
	 * name2[newline]
	 * ...
	 * nameN[newline]
	 * 
	 * @return A {@link List} object with the names of the Coordinator(s) (currently there is only one coordinator).
	 * @throws IOException 
	 */
	public List<String> loadCoordinatorNames(String filename) throws IOException{
		List<String> names = new LinkedList<String>();
		BufferedReader reader = null;
		String line = null;
		
		try{
			// Open the file to read
			reader = new BufferedReader(new FileReader(filename));
			
			// Read the lines from the file and store them in the names list
			while ( (line = reader.readLine()) != null ){
				names.add(line);
			}
		}
		finally{
			if (reader != null){
				reader.close();
			}
		}
		
		return names;
	}
}
