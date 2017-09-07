package gr.tuc.softnet.monitoring.protocol.coordinator;

/**
 * {@link CoordinatorIllegalStateException} indicates that an operation is to be executed at the {@link Coordinator} while it is in a state that 
 * does not permit that operation.
 * 
 * @author Tassos Souris
 */
@SuppressWarnings("serial")
public class CoordinatorIllegalStateException extends CoordinatorException{
	public CoordinatorIllegalStateException(){
		super();
	}
	
	public CoordinatorIllegalStateException(String msg){
		super(msg);
	}
	
	public CoordinatorIllegalStateException(Throwable s){
		super(s);
	}
	
	public CoordinatorIllegalStateException(String msg, Throwable s){
		super(msg, s);
	}
}
