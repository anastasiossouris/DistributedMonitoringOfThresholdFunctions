package gr.tuc.softnet.monitoring.protocol.coordinator;

import gr.tuc.softnet.monitoring.protocol.ordinarynode.OrdinaryNode;
import gr.tuc.softnet.monitoring.protocol.ordinarynode.OrdinaryNodeID;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * {@link Coordinator} is the remote object exposed by the Coordinator and used by the {@link OrdinaryNode}s to report constraint violations.
 * 
 * At the start, the {@link OrdinaryNode} must obtain a {@link OrdinaryNodeSession} object via a call to the session() method.
 * 
 * In order to implement the protocol the following messages are defined that the {@link OrdinaryNode}s send to the {@link Coordinator}.
 * 		(1) <INIT,Ui>
 * 		(2) <REP,Vi, Ui>
 * 
 * @author Tassos Souris
 */
public interface Coordinator extends Remote{
	
	/**
	 * Establishes a session between the {@link OrdinaryNode} and the {@link Coordinator}.
	 * 
	 * @return The {@link OrdinaryNodeSession} object for the {@link OrdinaryNode}.
	 * @param node The {@link OrdinaryNode} remote object of the node establishing a session.
	 * @param name The name of the node establishing a session.
	 * @throws RemoteException If an RMI error occurs.
	 * @throws CoordinatorException If a error occurs in the Coordinator state.
	 */
	OrdinaryNodeSession session(OrdinaryNode node, String name) throws RemoteException, CoordinatorException;
	
	/**
	 * Used by the nodes to report their initial statistics vector to the coordinator in the initialization stage.
	 * 
	 * @param id The {@link OrdinaryNodeID} of the {@link OrdinaryNode} making the call.
	 * @param localStatisticsVector The local statistics vector.
	 * @param weight The weight
	 * @throws RemoteException If an RMI error occurs.
	 * @throws CoordinatorException If a error occurs in the Coordinator state.
	 */
	void INIT(OrdinaryNodeID id, double [] localStatisticsVector, double weight) throws RemoteException, CoordinatorException;
	
	/**
	 * Used by nodes to report information to the coordinator when a local constraint has been violated, or when the coordinator requests information
	 * from a node.
	 * 
	 * @param id The {@link OrdinaryNodeID} of the {@link OrdinaryNode} making the call.
	 * @param driftVector The drift vector.
	 * @param localStatisticsVector The local statistics vector.
	 * @param weight The weight.
	 * @throws RemoteException If an RMI error occurs.
	 * @throws CoordinatorException If a error occurs in the Coordinator state.
	 */
	void REP(OrdinaryNodeID id, double [] driftVector, double [] localStatisticsVector, double weight) throws RemoteException, CoordinatorException;
}
