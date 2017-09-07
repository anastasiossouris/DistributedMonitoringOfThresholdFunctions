package gr.tuc.softnet.monitoring.protocol.ordinarynode.datastream;

import gr.tuc.softnet.monitoring.util.pair.Pair;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputSourceReader} is used to read from the {@link InputStream} as indicated by a {@link InputSource} object and derive from it
 * the value of the local statistics vector for this ordinary node.
 * 
 * Concrete classes must provide implementation for the derive() method.
 * 
 * @author Tassos Souris
 */
public abstract class InputSourceReader {
	private InputSource inputSource;
	
	public InputSourceReader(){
		this.inputSource = null;
	}
	
	public InputSourceReader(InputSource source){
		this.inputSource = source;
	}
	
	/**
	 * @param stream The {@link InputSource} to read from.
	 */
	public void setInputSource(InputSource source){
		this.inputSource = source;
	}
	
	/**
	 * @return The {@link InputSource}.
	 */
	public InputSource getInputSource(){
		return this.inputSource;
	}
	
	/**
	 * Reads from the input stream and derives the value of the local statistics vector. It also returns the weight assigned to the stream.
	 * 
	 * @return The local statistics vector as the first object of the pair and the weight assigned to the stream as the second object of the pair.
	 * @throws IOException
	 */
	public abstract Pair<double [], Double> derive() throws IOException;
}
