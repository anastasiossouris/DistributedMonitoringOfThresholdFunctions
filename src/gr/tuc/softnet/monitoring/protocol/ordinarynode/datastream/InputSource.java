package gr.tuc.softnet.monitoring.protocol.ordinarynode.datastream;

import java.io.InputStream;

/**
 * {@link InputSource} is used by a {@link InputSourceReader} to determine how to read input as a byte stream.
 * 
 * @author Tassos Souris
 */
public class InputSource {
	private InputStream byteStream;
	
	/**
	 * Create a {@link InputSource} with a null {@link InputStream}.
	 */
	public InputSource(){
		byteStream = null;
	}

	/**
	 * Create a {@link InputSource} with the given {@link InputStream} byte stream.
	 * 
	 * @param byteStream The {@link InputStream}.
	 */
	public InputSource(InputStream byteStream){
		this.byteStream = byteStream;
	}
	
	/**
	 * @param byteStream The {@link InputStream} to use as a byte stream.
	 */
	public void setByteStream(InputStream byteStream){
		this.byteStream = byteStream;
	}
	
	/**
	 * @return The {@link InputStream} used as a byte stream.
	 */
	public InputStream getByteStream(){
		return this.byteStream;
	}
}
