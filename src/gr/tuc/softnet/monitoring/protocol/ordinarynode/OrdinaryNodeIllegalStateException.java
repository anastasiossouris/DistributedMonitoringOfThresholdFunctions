package gr.tuc.softnet.monitoring.protocol.ordinarynode;

/**
 * {@link OrdinaryNodeIllegalStateException} indicates that an action is to be executed on the {@link OrdinaryNode} object 
 * that violates its state properties.
 * 
 * @author Tassos Souris
 */
@SuppressWarnings("serial")
public class OrdinaryNodeIllegalStateException extends OrdinaryNodeException{
	public OrdinaryNodeIllegalStateException(){
		super();
	}
	
	public OrdinaryNodeIllegalStateException(String msg){
		super(msg);
	}
	
	public OrdinaryNodeIllegalStateException(String msg,Throwable s){
		super(msg,s);
	}
	
	public OrdinaryNodeIllegalStateException(Throwable s){
		super(s);
	}
}
