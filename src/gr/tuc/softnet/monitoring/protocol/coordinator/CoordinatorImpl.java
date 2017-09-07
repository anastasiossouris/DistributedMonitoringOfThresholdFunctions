package gr.tuc.softnet.monitoring.protocol.coordinator;

import gr.tuc.softnet.monitoring.protocol.constraint.Ball;
import gr.tuc.softnet.monitoring.protocol.constraint.MonitoredFunction;
import gr.tuc.softnet.monitoring.protocol.ordinarynode.OrdinaryNode;
import gr.tuc.softnet.monitoring.protocol.ordinarynode.OrdinaryNodeID;
import gr.tuc.softnet.monitoring.util.buffer.Buffer;
import gr.tuc.softnet.monitoring.util.pair.Pair;
import gr.tuc.softnet.monitoring.util.vector.VectorOps;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author Tassos Souris
 */
public class CoordinatorImpl implements Coordinator{
	private Set<String> names = null; // the names of the nodes we expect to receive
	private CoordinatorSharedState sharedState = null; // the shared state we keep with the balancer
	
	private Set<OrdinaryNodeID> ids = null; // needed to check when we finish the init state
	private Map<OrdinaryNodeID, OrdinaryNode> idsToNodesMap = null; // a map from the ids of the nodes to their remote objects.
	private Map<OrdinaryNodeID,double []> idsToLocalStatisticsVectors = null; // a map from the ids of the nodes to the last statistics they send
	private Map<OrdinaryNodeID,Double> idsToWeights = null; // a map from the ids of the nodes to the last weights they send
	
	private MonitoredFunction monitoredFunction = null; // the monitored function we have
	
	// needed for the balancing process... refer to REP() and the balancer for more info
	private Buffer<OrdinaryNodeID> waitingFromIDBuffer = null;
	private Buffer<REPValues> waitingREPValuesBuffer = null;
	
	public CoordinatorImpl(){
		names = new HashSet<String>();
		ids = new HashSet<OrdinaryNodeID>();
		sharedState = new CoordinatorSharedState();
		idsToNodesMap = new HashMap<OrdinaryNodeID, OrdinaryNode>();
		idsToLocalStatisticsVectors = new HashMap<OrdinaryNodeID, double[]>();
		idsToWeights = new HashMap<OrdinaryNodeID, Double>();
		waitingFromIDBuffer = new Buffer<OrdinaryNodeID>();
		waitingREPValuesBuffer = new Buffer<CoordinatorImpl.REPValues>();
		
		// we start in the INIT state
		sharedState.setCoordinatorState(CoordinatorState.INIT);
	}
	
	/**
	 * @param names  The set of names we expect to receive session requests.
	 */
	public void setOrdinayNodeNames(Set<String> names){
		this.names = names;
	}
	
	/**
	 * @param monitoredFunction The {@link MonitoredFunction} for this monitoring task.
	 */
	public void setMonitoredFunction(MonitoredFunction monitoredFunction){
		this.monitoredFunction = monitoredFunction;
	}
	
	/**
	 * Establishes a session between the {@link OrdinaryNode} and the {@link Coordinator}.
	 */
	public synchronized OrdinaryNodeSession session(OrdinaryNode node, String name)
			throws RemoteException, CoordinatorException {
		/**
		 * The ordinary nodes establish a session with the coordinator in the initialization phase 
		 * so we must now be at the INIT state.
		 */
		if (sharedState.getCoordinatorState() != CoordinatorState.INIT){
			throw new CoordinatorIllegalStateException();
		}
		
		/**
		 * We have a set of ordinary nodes that we must wait for the monitoring task to proceed.
		 * The names of those nodes are kept in the names set and each node sends us its name in the second parameter of the session() method.
		 * 
		 * Upon receipt of a session request we must check if the name we are given exists or not in the set.
		 * If it not exists then either the node has already established a session or we are not waiting for a node with this name.
		 * In either case this is a illegal state and we throw a exception.
		 * To check if all of the nodes have established a session each time we receive a session request from a node we remove from the 
		 * set the corresponding name. So, when we reach a count of zero in the set this means that all nodes have established a session with us.
		 */
		if (!names.contains(name)){
			throw new CoordinatorIllegalStateException();
		}
		
		names.remove(name);
		
		/**
		 * We must create a session object to return to the client node.
		 * This means generating a ID for the node and a ordinary node session as a wrapper to the coordinator.
		 */
		OrdinaryNodeID id = new OrdinaryNodeID();
		OrdinaryNodeSession session = new OrdinaryNodeSession(this, id);
		
		/**
		 * Remember the node 
		 */
		idsToNodesMap.put(id, node);
		
		/**
		 * Remember the id. This is needed for the INIT() method. See there for more info
		 */
		ids.add(id);
		
		return session;
	}

	/**
	 * Used by the nodes to report their initial statistics vector and weight to the coordinator in the initialization stage.
	 */
	public synchronized void INIT(OrdinaryNodeID id, double[] localStatisticsVector,
			double weight) throws RemoteException, CoordinatorException {
		/**
		 * We must be in the INIT state now
		 */
		if (sharedState.getCoordinatorState() != CoordinatorState.INIT){
			throw new CoordinatorIllegalStateException();
		}
		
		/**
		 * The node must have first established a session with the session() method.
		 * 
		 * Whenever a node receives a session object we keep its id in the ids set. So the ids set contains the ids of all the nodes
		 * from which we expect to receive a INIT() message.
		 * If the id is not contained in the set this means that either we have an invalid session or that the node called INIT() twice. In either case,
		 * this is a illegal state. 
		 * If the id is contained in the set then we must record the INIT message (see later) and also remove the id from the set
		 * to indicate that the node has send the INIT() message
		 */
		if (!ids.contains(id)){
			throw new CoordinatorIllegalStateException();
		}
		
		/**
		 * We must remember the vectors send by the node. We keep those in the idsToValuesMap
		 */
		idsToLocalStatisticsVectors.put(id, localStatisticsVector);
		idsToWeights.put(id, weight);
		
		/**
		 * remove the id from the set
		 */
		ids.remove(id);
		
		/**
		 * If the set reached count zero this means that we received the INIT() messages from all the nodes
		 */
		if (ids.isEmpty()){
			/**
			 * Now we must calculate the estimate vector and inform all of the nodes with a NEW-EST message.
			 */
			
			InitialNEW_ESTSender sender = new InitialNEW_ESTSender();
			
			CountDownLatch startUpLatch = new CountDownLatch(1);
			
			sender.setCoordinatorSharedState(sharedState);
			sender.setIdsToLocalStatisticsVectors(idsToLocalStatisticsVectors);
			sender.setIdsToWeights(idsToWeights);
			sender.setMonitoredFunction(monitoredFunction);
			sender.setNodes(idsToNodesMap);
			sender.setStartUpLatch(startUpLatch);
			
			(new Thread(sender)).start();
			
			startUpLatch.countDown();
		}
	}

	/**
	 * Used by nodes to report information to the coordinator when a local constraint has been violated, or when the coordinator requests information
	 * from a node.
	 */ 
	public synchronized void REP(OrdinaryNodeID id, double[] driftVector,
			double[] localStatisticsVector, double weight)
			throws RemoteException, CoordinatorException {
		/**
		 * First we must check that we have a session for the given id and also that we are not in the INIT state
		 */
		if (!idsToNodesMap.containsKey(id) || sharedState.getCoordinatorState() == CoordinatorState.INIT){
			throw new CoordinatorIllegalStateException();
		}
		
		/**
		 * We distinguish between two cases here:
		 * 
		 * 1) We are in the BALANCED state
		 * 		A local constraint has been violated in the node and we must initiate a balancing process
		 * 
		 * 2) We are in the UNBALANCED state
		 * 		A balancing process is in progress and we receive REP() messages from the nodes we send a REQ() message to.
		 */
		if (sharedState.getCoordinatorState() == CoordinatorState.BALANCED){
			CountDownLatch startUpLatch = new CountDownLatch(1);
			
			/**
			 * We must initiate a balancing process now
			 */
			BalancingProcess balancingProcess = new BalancingProcess();
			balancingProcess.setCoordinatorSharedState(this.sharedState);
			balancingProcess.setIdsToNodesMap(idsToNodesMap);
			balancingProcess.setMonitoredFunction(monitoredFunction);
			balancingProcess.setStartupLatch(startUpLatch);
			balancingProcess.setWaitingFromIDBuffer(waitingFromIDBuffer);
			balancingProcess.setWaitingREPValuesBuffer(waitingREPValuesBuffer);
			balancingProcess.setStartingOrdinaryNode(
					new Pair<OrdinaryNodeID,REPValues>(id,new REPValues(driftVector, localStatisticsVector, weight))
					);
			
			(new Thread(balancingProcess)).start();
			
			startUpLatch.countDown();
		}
		else{
			/**
			 * Here we are unbalanced...
			 * 
			 * We can receive a REP() message only in the case where during the balancing process the ball is found to be 
			 * not monochromatic in which case if there are nodes not contained in the balancing group, one of them is selected 
			 * at random and we send it a REQ() message. The node then replies with a REP() message.
			 * 
			 * So we must check that the balancer told us that it is waiting for the values of a REP() message and from which node (given its ID).
			 * This is done through the buffer waitingFromIDBuffer where if it its not empty it contains the id of the node from which
			 * the balancer awaits the a REP() message
			 */
			try {
				if (waitingFromIDBuffer.isEmpty() || waitingFromIDBuffer.get() != id){
					throw new CoordinatorIllegalStateException();
				}
			} catch (InterruptedException e) {
			}
			
			/**
			 * Now that we have got the REP() message from the node we give the balancer the values it wants using the waitingREPValuesBuffer.
			 */
			try {
				waitingREPValuesBuffer.put(new REPValues(driftVector, localStatisticsVector, weight));
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * {@link BalancingProcess} implements the balancing process logic.
	 * 
	 * @author Tassos Souris
	 */
	class BalancingProcess implements Runnable{
		private CoordinatorSharedState sharedState; // the shared state we keep with the remote object.
		private MonitoredFunction monitoredFunction; // the monitored function we have
		private Buffer<OrdinaryNodeID> waitingFromIDBuffer; // see REP() and later in the run() method
		private Buffer<REPValues> waitingREPValuesBuffer; // see REP() and later in the run() method
		private CountDownLatch startUpLatch; // latch to indicate when to start
		private Pair<OrdinaryNodeID,REPValues> startingOrdinaryNode; // the intiial ordinary node that send the REP() message and initiated the balancing process
		private Map<OrdinaryNodeID, OrdinaryNode> idsToNodesMap = null; // a map from the ids of the nodes to their remote objects.
		
		public BalancingProcess(){
			
		}
		
		public void setCoordinatorSharedState(CoordinatorSharedState sharedState){
			this.sharedState = sharedState;
		}
		
		public void setMonitoredFunction(MonitoredFunction monitoredFunction){
			this.monitoredFunction = monitoredFunction;
		}
		
		public void setWaitingFromIDBuffer(Buffer<OrdinaryNodeID> waitingFromIDBuffer){
			this.waitingFromIDBuffer = waitingFromIDBuffer;
		}
		
		public void setWaitingREPValuesBuffer(Buffer<REPValues> waitingREPValuesBuffer){
			this.waitingREPValuesBuffer = waitingREPValuesBuffer;
		}
		
		public void setStartupLatch(CountDownLatch startUpLatch){
			this.startUpLatch = startUpLatch;
		}
		
		public void setStartingOrdinaryNode(Pair<OrdinaryNodeID,REPValues> startingOrdinaryNode){
			this.startingOrdinaryNode = startingOrdinaryNode;
		}
		
		public void setIdsToNodesMap(Map<OrdinaryNodeID, OrdinaryNode> idsToNodesMap){
			this.idsToNodesMap = idsToNodesMap;
		}
		
		/**
		 * Execute a balancing process
		 */
		public void run() {
			try{
				// start when we are told to do so...
				startUpLatch.await();
				
				// we maintain two groups.. the balancing group and the non balancing group
				// for each group we must keep the remote object and the last values we received from them
				Map<OrdinaryNodeID,OrdinaryNode> balancingGroupNodes = new HashMap<OrdinaryNodeID,OrdinaryNode>();
				Map<OrdinaryNodeID,REPValues> balancingGroupValues = new HashMap<OrdinaryNodeID,REPValues>();
				
				Map<OrdinaryNodeID,OrdinaryNode> notBalancingGroupNodes = new HashMap<OrdinaryNodeID,OrdinaryNode>();
				
				// at first in the non balancing group are all the nodes that have a session with the coordinator
				// except the one that send the REP() message and initiated the balancing process...
				notBalancingGroupNodes.putAll(this.idsToNodesMap);
				// remove the starting one....
				notBalancingGroupNodes.remove(startingOrdinaryNode.getFirst());
				
				// in the balancing group we put the starting node
				balancingGroupNodes.put(startingOrdinaryNode.getFirst(), this.idsToNodesMap.get(startingOrdinaryNode.getFirst()));
				balancingGroupValues.put(startingOrdinaryNode.getFirst(),startingOrdinaryNode.getSecond());
				
				
				while (true){
					/**
					 * calculate the balanced vector b
					 * 
					 * b = SUM( nodeWeight[node]*nodeDrift[node] 
					 * 	for node in balancing_group)  / 
					 * 	SUM( nodeWeight[node] for node in balancing_group) 
					 */
					double[] b = new double[monitoredFunction.getVectorSpace().getDimension()];
					double W = 0.0;
					Arrays.fill(b, 0.0);
					for(OrdinaryNodeID nodeID: balancingGroupNodes.keySet()) {
						double w = balancingGroupValues.get(nodeID).getWeight();
						VectorOps.multAndAddTo(b, w, balancingGroupValues.get(nodeID).getDriftVector());
						W += w;
					}
					VectorOps.multBy(b, 1.0/W);
					
					// check if the ball is monochromatic
					if (this.monitoredFunction.getIsMonochromaticSpecification().IsSatisfiedBy(new Ball(sharedState.getEstimateVector(), b))){
						/**
						 * For each ordinary node in the balancing group we must calculate the slack vector adjustment and send a ADJ-SLK message 
						 * to each of the nodes
						 */
						for(OrdinaryNodeID nodeID: balancingGroupNodes.keySet()) {
							/**
							 * Compute deltaSlack
							 * 
							 * deltaSlack[node] = nodeWeight[node] *
							 * 	(b - nodeDrift[node])
							 */
							double weight = balancingGroupValues.get(nodeID).getWeight();
							double[] drift = balancingGroupValues.get(nodeID).getDriftVector();
							double[] deltaSlack = VectorOps.sub(b, drift);
							VectorOps.multBy(deltaSlack, weight);

							// send deltaSlack
							balancingGroupNodes.get(nodeID).ADJ_SLK(deltaSlack);
						}
						
						/**
						 * Since balancing is achieved we change state to BALANCED
						 */
						this.sharedState.setCoordinatorState(CoordinatorState.BALANCED);
						
						/**
						 * exit the balancing process now...
						 */
						break;
					}
					else{
						/**
						 * If there are nodes not contained in the balancing group, we must select one of these nodes at random
						 * and send it a REQ message.
						 */
						Set<OrdinaryNodeID> _nodes = balancingGroupNodes.keySet();
						List<OrdinaryNodeID> nodes = new LinkedList<OrdinaryNodeID>(_nodes);
						
						if (!nodes.isEmpty()){
							/**
							 * select one of the nodes at random
							 */
							Random random = new Random();
							int next = random.nextInt(nodes.size());
							
							OrdinaryNodeID nodeID = nodes.get(next);
							OrdinaryNode node = notBalancingGroupNodes.get(nodeID);
							
							/**
							 * remove the node from the not balancing group and add it to the balancing group
							 */
							notBalancingGroupNodes.remove(nodeID);
							balancingGroupNodes.put(nodeID, node);
							
							/**
							 * Send a REQ() message to the node
							 * First, indicate that we are expecting a REP() message from this node
							 */
							this.waitingFromIDBuffer.put(nodeID);
							node.REQ();
							/**
							 * Wait for the reply of the node
							 */
							balancingGroupValues.put(nodeID, this.waitingREPValuesBuffer.get());
							
							/**
							 * continue with the balancing
							 */
							continue;
						}
						else{
							/**
							 * Otherwise  we must calculate a new estimate vector and send a NEW-EST message to all nodes.
							 */
							
							/**
							 * globalEstimate = SUM( nodeWeight[node] * localEstimate[node] 
							 * 	for node in nodes ) /
							 * 	SUM( nodeWeight[node]  for node in nodes )
							 */
							double [] globalEstimate = new double[this.monitoredFunction.getVectorSpace().getDimension()];
							Arrays.fill(globalEstimate, 0.0);
							double sumw = 0.0;

							for(OrdinaryNodeID nodeID: balancingGroupNodes.keySet()) {
								double w = balancingGroupValues.get(nodeID).getWeight();
								sumw+=w;
								VectorOps.addTo(globalEstimate, 
										VectorOps.mult(balancingGroupValues.get(nodeID).getLocalStatisticsVector(), w));
							}
							VectorOps.multBy(globalEstimate, 1.0/sumw);
							
							/**
							 * update the shared state and send the NEW-EST message
							 */
							this.sharedState.setEstimateVector(globalEstimate.clone());
							for (OrdinaryNode node : balancingGroupNodes.values()){
								node.NEW_EST(globalEstimate);
							}
							this.sharedState.setCoordinatorState(CoordinatorState.BALANCED);
							
							/**
							 * Exit the balancing process now...
							 */
							break;
						}
					}
				}
			}
			catch( Exception e ){	
			}	
		}
		
	}
	
	/**
	 * @author Tassos Souris
	 */
	class InitialNEW_ESTSender implements Runnable{
		private MonitoredFunction monitoredFunction;
		private CoordinatorSharedState sharedState;
		private Map<OrdinaryNodeID,OrdinaryNode> nodes;
		private Map<OrdinaryNodeID,double []> idsToLocalStatisticsVectors = null; // a map from the ids of the nodes to the last statistics they send
		private Map<OrdinaryNodeID,Double> idsToWeights = null; // a map from the ids of the nodes to the last weights they send
		private CountDownLatch startUpLatch  = null;
		
		public InitialNEW_ESTSender(){
			
		}
		
		public void setMonitoredFunction(MonitoredFunction monitoredFunction){
			this.monitoredFunction = monitoredFunction;
		}
		
		public void setCoordinatorSharedState(CoordinatorSharedState sharedState){
			this.sharedState = sharedState;
		}

		public void setNodes(Map<OrdinaryNodeID,OrdinaryNode> nodes){
			this.nodes = nodes;
		}
		
		public void setIdsToLocalStatisticsVectors(Map<OrdinaryNodeID,double []> idsToLocalStatisticsVectors){
			this.idsToLocalStatisticsVectors = idsToLocalStatisticsVectors;
		}
		
		public void setIdsToWeights(Map<OrdinaryNodeID,Double> idsToWeights){
			this.idsToWeights = idsToWeights;
		}
		
		public void setStartUpLatch(CountDownLatch startUpLatch){
			this.startUpLatch = startUpLatch;
		}
		
		public void run() {
			try{
				startUpLatch.await();
				
				/**
				 * globalEstimate = SUM( nodeWeight[node] * localEstimate[node] 
				 * 	for node in nodes ) /
				 * 	SUM( nodeWeight[node]  for node in nodes )
				 */
				double [] globalEstimate = new double[this.monitoredFunction.getVectorSpace().getDimension()];
				Arrays.fill(globalEstimate, 0.0);
				double sumw = 0.0;
	
				for(OrdinaryNodeID nodeID: nodes.keySet()) {
					double w = idsToWeights.get(nodeID);
					sumw+=w;
					VectorOps.addTo(globalEstimate, 
							VectorOps.mult(idsToLocalStatisticsVectors.get(nodeID), w));
				}
				VectorOps.multBy(globalEstimate, 1.0/sumw);
				
				/**
				 * update the shared state and send the NEW-EST message
				 */
				this.sharedState.setEstimateVector(globalEstimate.clone());
				for (OrdinaryNode node : nodes.values()){
					node.NEW_EST(globalEstimate);
				}
				this.sharedState.setCoordinatorState(CoordinatorState.BALANCED);
			}
			catch(Exception e){
				
			}
		}
		
	}
	
	/**
	 * {@link CoordinatorSharedState} keeps the state we need to maintain thread-safely between the {@link Coordinator} remote object
	 * and the balancing process.
	 * 
	 * @author Tassos Souris
	 */
	class CoordinatorSharedState{
		private double [] estimateVector;
		private CoordinatorState state;
		
		public CoordinatorSharedState(){
			
		}
		
		public synchronized void setEstimateVector(double [] estimateVector){
			this.estimateVector = estimateVector;
		}
		
		public synchronized void setCoordinatorState(CoordinatorState state){
			this.state = state;
		}
		
		public synchronized double [] getEstimateVector(){
			return this.estimateVector;
		}
		
		public synchronized CoordinatorState getCoordinatorState(){
			return this.state;
		}
	}
	
	/**
	 * {@link REPValues} is a wrapper around the values we receive in the REP() message.
	 * 
	 * @author Tassos Souris
	 */
	class REPValues{
		private double[] driftVector;
		private double[] localStatisticsVector;
		private double weight;
		
		
		public REPValues(){
			
		}
		
		public REPValues(double [] driftVector, double [] localStatisticsVector, double weight){
			this.driftVector = driftVector;
			this.localStatisticsVector = localStatisticsVector;
			this.weight = weight;
		}
		
		public void setDrfitVector(double [] driftVector){
			this.driftVector = driftVector;
		}
		
		public void setLocalStatisticsVector(double [] localStatisticsVector){
			this.localStatisticsVector = localStatisticsVector;
		}
		
		public void setWeight(double weight){
			this.weight = weight;
		}
		
		public double [] getDriftVector(){
			return this.driftVector;
		}
		
		public double [] getLocalStatisticsVector(){
			return this.localStatisticsVector;
		}
		
		public double getWeight(){
			return this.weight;
		}
	}
}
