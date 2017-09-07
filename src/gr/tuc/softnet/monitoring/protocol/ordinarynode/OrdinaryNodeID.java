package gr.tuc.softnet.monitoring.protocol.ordinarynode;

import java.util.UUID;

/**
 * {@link OrdinaryNodeID} is used to assign a unique id to each ordinary node.
 * 
 * @author Tassos Souris
 */
public class OrdinaryNodeID {
	UUID id;
	
	/**
	 * Construct a new OrdinaryNodeID object with a random id.
	 */
	public OrdinaryNodeID(){
		this.id = UUID.randomUUID();
	}
	
	/**
	 * Construct a new OrdinaryNodeID object with the given id.
	 */
	public OrdinaryNodeID(UUID id){
		this.id = id;
	}
	
	/**
	 * @param The id to set.
	 */
	public void setID(UUID id){
		this.id = id;
	}
	
	/**
	 * @return The id.
	 */
	public UUID getID(){
		return this.id;
	}
}
