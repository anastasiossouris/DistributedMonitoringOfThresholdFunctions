package gr.tuc.softnet.monitoring.protocol.ordinarynode.datastream;

import gr.tuc.softnet.monitoring.protocol.ordinarynode.OrdinaryNode;
import gr.tuc.softnet.monitoring.util.pair.Pair;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * {@link DataStream} represents the incoming data stream to the {@link OrdinaryNode}. 
 * 
 * The incoming data stream is represented by a {@link InputSource} and a {@link InputSourceReader} is used to derive from the incoming stream 
 * the values of the local statistics vector as well as the weight assigned to the stream. 
 * 
 * The {@link OrdinaryNode} is notified for the local statistics vectors as:
 * 		The {@link DataStream} runs in its own thread receiving from the incoming data stream and deriving from it the local statistics vectors.
 * 		To accomplish the communication with the {@link OrdinaryNode}, we use a {@link BlockingQueue} implementing a Producer/Consumer pattern, 
 * 		where the producer is the {@link DataStream} that puts the values of the vectors in the queue and the {@link OrdinaryNode} (meaning another thread)
 * 		playing the role of the consumer that takes the values from the queue. 
 * 
 * @author Tassos Souris
 */
public class DataStream implements Callable<Boolean>{
	private BlockingQueue<Pair<double [], Double>> queue; // the blocking queue to store the values into
	private InputSourceReader reader; // the reader to get values from the input stream
	
	public DataStream(){
		
	}
	
	/**
	 * @param queue The {@link BlockingQueue}.
	 */
	public void setBlockingQueue(BlockingQueue<Pair<double [], Double>> queue){
		this.queue = queue;
	}
	
	/**
	 * @param reader The {@link InputSourceReader}.
	 */
	public void setInputSourceReader(InputSourceReader reader){
		this.reader = reader;
	}
	
	/**
	 * @return The blocking queue to store values into.
	 */
	public BlockingQueue<Pair<double [], Double>> getBlockingQueue(){
		return queue;
	}
	
	/**
	 * @return The {@link InputSourceReader}.
	 */
	public InputSourceReader getInputSourceReader(){
		return reader;
	}
	
	/**
	 * Start receiving values from the input stream and derive from them the values of the
	 * local statistics vector. Store these values in the queue.
	 * 
	 */
	public Boolean call() throws InterruptedException, IOException {
		while(true){
			queue.put(reader.derive());
		}
	}

}
