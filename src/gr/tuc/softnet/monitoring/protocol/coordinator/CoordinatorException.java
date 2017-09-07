package gr.tuc.softnet.monitoring.protocol.coordinator;

/**
 * {@link CoordinatorException} indicates a exception in the state of the {@link Coordinator}.
 * 
 * @author Tassos Souris
 */
@SuppressWarnings("serial")
public class CoordinatorException extends Exception{
	public CoordinatorException(){
		super();
	}
	
	public CoordinatorException(String msg){
		super(msg);
	}
	
	public CoordinatorException(Throwable s){
		super(s);
	}
	
	public CoordinatorException(String msg, Throwable s){
		super(msg, s);
	}
}
