package gr.tuc.softnet.monitoring.protocol.coordinator;

/**
 * {@link CoordinatorState} represents the state that the {@link Coordinator} is currently at.
 * 
 * @author Tassos Souris
 */
public enum CoordinatorState {
	/**
	 * The coordinator waits for the ordinary nodes to register and to receive a INIT message from all of them.
	 */
	INIT,
	/**
	 * No constraint violation has been reported to the coordinator.
	 */
	BALANCED,
	/**
	 * A ordinary node has sent a REP message to the coordinator and it is executing a balancing process.
	 */
	UNBALANCED
}
