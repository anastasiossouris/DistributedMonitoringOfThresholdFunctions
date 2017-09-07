package gr.tuc.softnet.monitoring.protocol.ordinarynode;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * {@link OrdinaryNode} is the remote object exposed by each of the ordinary nodes and used by the Coordinator to contact them.
 * 
 * The Coordinator needs to communicate with the OrdinaryNodes during the initialization phase and during a balancing process.
 * In the initialization phase each OrdinaryNode sends to the coordinator a INIT message. Upon receipt of messages from all the nodes, the 
 * coordinator calculates the estimate vector and informs the nodes via a NEW-EST message.
 * During the balancing process, the coordinator sends a REQ message to OrdinaryNodes to obtain their statistics vector and drift vector.
 * At the end of the balancing process, the coordinator either sends a new estimate vector to all of the OrdinaryNodes via a NEW-EST message, or 
 * sends slack vector adjustments to a subset of the OrdinaryNodes via a ADJ-SLK message.
 * 
 * The Coordinator to send a NEW-EST message to a OrdinaryNode calls the NEW_EST() method on the OrdinaryNode remote object.
 * To send a ADJ-SLK message it calls the ADJ_SLK() method and to send a REQ message the REQ() method.
 * 
 * @author Tassos Souris
 */
public interface OrdinaryNode extends Remote{
	
	/**
	 * Used by the coordinator to report a new estimate vector to the OrdinaryNode.
	 * 
	 * @param estimateVector The new estimate vector
	 */
	public void NEW_EST(double [] estimateVector) throws RemoteException, OrdinaryNodeException;
	
	/**
	 * Used by the coordinator to report slack vector adjustment to the OrdinaryNode after a successful balancing process.
	 * 
	 * @param slackVectorAdjustment The slack vector adjustment
	 */
	public void ADJ_SLK(double [] slackVectorAdjustment) throws RemoteException, OrdinaryNodeException;
	
	/**
	 * Used by the coordinator during the balancing process to request that a node send its statistics vector and drift vector.
	 */
	public void REQ() throws RemoteException, OrdinaryNodeException;
}
