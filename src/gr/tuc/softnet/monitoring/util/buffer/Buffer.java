package gr.tuc.softnet.monitoring.util.buffer;

/**
 * {@link Buffer} is used when we need to wait for input and act on an object the moment it is added to a Buffer.
 * To achieve this we need to block until the buffer is filled with input.
 * 
 * @author Tassos Souris
 */
public class Buffer<T> {
	private T contents;
	private boolean empty = true;
	
	/**
	 * Add the object in the buffer only if the buffer is empty. If not wait until the object currently in the buffer
	 * is retrieved with a call to the get() method.
	 * 
	 * @param item The object to add in the buffer.
	 */
	public synchronized void put (T item) throws InterruptedException { 
		while (empty == false) { 	//wait till the buffer becomes empty
			try { wait(); }
			catch (InterruptedException e) {throw e;}
		}
		contents = item;
		empty = false;
		notify();
	} 
	
	/**
	 * Retrieve the next object in the buffer waiting until an object is added if the buffer is empty.
	 * 
	 * @return The next object in the buffer.
	 */
	public synchronized T get () throws InterruptedException {
		while (empty == true)  {	//wait till something appears in the buffer
			try { 	wait(); }
			catch (InterruptedException e) {throw e;}
		}
		empty = true;
		notify();
		return contents;
	}
	
	/**
	 * @return True if the buffer is empty and false otherwise.
	 */
	public synchronized boolean isEmpty(){
		return empty;
	}
}
