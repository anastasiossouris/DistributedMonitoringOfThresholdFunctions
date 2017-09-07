package gr.tuc.softnet.monitoring.protocol.coordinator;

import java.rmi.RemoteException;

import gr.tuc.softnet.monitoring.protocol.ordinarynode.OrdinaryNode;
import gr.tuc.softnet.monitoring.protocol.ordinarynode.OrdinaryNodeID;

/**
 * {@link OrdinaryNodeSession} represents a session between the {@link Coordinator} and a {@link OrdinaryNode} object.
 * 
 * @author Tassos Souris
 */
public class OrdinaryNodeSession {
	private Coordinator coord;
	private OrdinaryNodeID id;
	
	public OrdinaryNodeSession(){
		this.coord = null;
		this.id = null;
	}
	
	public OrdinaryNodeSession(Coordinator coord, OrdinaryNodeID id){
		this.coord = coord;
		this.id = id;
	}
	
	/**
	 * @param coord The {@link Coordinator} object.
	 */
	public void setCoordinator(Coordinator coord){
		this.coord = coord;
	}
	
	/**
	 * @param id The {@link OrdinaryNodeID} object.
	 */
	public void setOrdinaryNodeID(OrdinaryNodeID id){
		this.id = id;
	}
	
	/**
	 * @return The {@link Coordinator} object.
	 */
	public Coordinator getCoordinator(){
		return this.coord;
	}
	
	/**
	 * @return The {@link OrdinaryNodeID} object.
	 */
	public OrdinaryNodeID getOrdinaryNodeID(){
		return this.id;
	}
	
	/**
	 * Wrapper to the INIT() method of the {@link Coordinator}.
	 */
	public void INIT(double [] localStatisticsVector, double weight) throws RemoteException, CoordinatorException{
		this.coord.INIT(id, localStatisticsVector, weight);
	}
	
	/**
	 * Wrapper to the REP() method of the {@link Coordinator}
	 */
	public void REP(double [] driftVector, double [] localStatisticsVector, double weight) throws RemoteException, CoordinatorException{
		this.coord.REP(id, driftVector, localStatisticsVector, weight);
	}
}
