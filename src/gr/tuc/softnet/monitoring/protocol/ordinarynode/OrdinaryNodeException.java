package gr.tuc.softnet.monitoring.protocol.ordinarynode;

/**
 * {@link OrdinaryNodeException} indicates a error in an {@link OrdinaryNode} object.
 * 
 * @author Tassos Souris
 */
@SuppressWarnings("serial")
public class OrdinaryNodeException extends Exception{
	public OrdinaryNodeException(){
		super();
	}
	
	public OrdinaryNodeException(String msg){
		super(msg);
	}
	
	public OrdinaryNodeException(String msg, Throwable s){
		super(msg, s);
	}
	
	public OrdinaryNodeException(Throwable s){
		super(s);
	}
}
