package gr.tuc.softnet.monitoring.services;

import java.io.InputStream;

import gr.tuc.softnet.monitoring.protocol.constraint.MonitoredFunction;
import gr.tuc.softnet.monitoring.protocol.ordinarynode.datastream.InputSourceReader;

/**
 * {@link ServicesFactory} is a singleton factory to retrieve various objects needed at runtime.
 * 
 * @author Tassos Souris
 */
public class ServicesFactory {
	public static final String MONITORED_FUNCTION_FACTORY_PROPERTY_NAME = "monitoredfunction.class.name";
	public static final String INPUT_STREAM_FACTORY_PROPERTY_NAME = "inputstream.class.name";
	public static final String INPUT_SOURCE_READER_FACTORY_PROPERTY_NAME = "inputsourcereader.class.name";
	
	/**
	 * the singleton services factory instance
	 */
	private static ServicesFactory instance = null;
	
	
	private static MonitoredFunction monitoredFunction = null;
	private static InputStream inputStream = null;
	private static InputSourceReader inputSourceReader = null;
	
	private ServicesFactory(){
		
	}
	
	/**
	 * @return The {@link ServicesFactory} singleton object.
	 */
	public static synchronized ServicesFactory getInstance(){
		if (instance == null){
			instance = new ServicesFactory();
		}
		return instance;
	}
	
	/**
	 * @return The {@link MonitoredFunction} for the monitoring task.
	 */
	public synchronized MonitoredFunction getMonitoredFunction() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		if (monitoredFunction == null){
			String className = System.getProperty(MONITORED_FUNCTION_FACTORY_PROPERTY_NAME);
			monitoredFunction = (MonitoredFunction)Class.forName(className).newInstance();
		}
		return monitoredFunction;
	}
	
	/**
	 * @return The {@link InputStream}.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public synchronized InputStream getInputStream() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		if (inputStream == null){
			String className = System.getProperty(INPUT_STREAM_FACTORY_PROPERTY_NAME);
			inputStream = (InputStream)Class.forName(className).newInstance();
		}
		return inputStream;
	}
	
	/**
	 * @return The {@link InputSourceReader} for the data stream.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public synchronized InputSourceReader getInputSourceReader() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		if (inputSourceReader == null){
			String className = System.getProperty(INPUT_SOURCE_READER_FACTORY_PROPERTY_NAME);
			inputSourceReader = (InputSourceReader)Class.forName(className).newInstance();
		}
		return inputSourceReader;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
