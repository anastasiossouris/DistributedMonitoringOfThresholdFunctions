package gr.tuc.softnet.monitoring.protocol.ordinarynode;

import gr.tuc.softnet.monitoring.protocol.constraint.Ball;
import gr.tuc.softnet.monitoring.protocol.constraint.MonitoredFunction;
import gr.tuc.softnet.monitoring.protocol.coordinator.CoordinatorException;
import gr.tuc.softnet.monitoring.protocol.coordinator.OrdinaryNodeSession;
import gr.tuc.softnet.monitoring.protocol.ordinarynode.datastream.DataStream;
import gr.tuc.softnet.monitoring.util.pair.Pair;
import gr.tuc.softnet.monitoring.util.vector.VectorOps;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * {@link OrdinaryNodeInternalState} implements the {@link OrdinaryNode} interface.
 * 
 * @author Tassos Souris
 */
public class OrdinaryNodeImpl implements OrdinaryNode{
	private OrdinaryNodeSession session = null; // the session that we have established with the coordinator
	private MonitoredFunction monitoredFunction = null; // the monitored function we use
	private BlockingQueue<Pair<double [], Double>> queue = null; // the queue from where we receive from the input data stream
	private DataStreamConsumer dataStreamConsumer = null; // the data stream consumer
	private OrdinaryNodeSharedState sharedState = null; // the shared state 
	private CountDownLatch startUpLatch = null; // latch for the data stream consumer to start
	
	public OrdinaryNodeImpl(){
	}
	
	/**
	 * Initialize the ordinary node. We assume that the setters have already been called.
	 * @throws CoordinatorException 
	 * @throws RemoteException 
	 */
	public void init() throws RemoteException, CoordinatorException{
		// Create the shared state object
		sharedState = new OrdinaryNodeSharedState();
		sharedState.setOrdinaryNodeState(OrdinaryNodeState.INIT);
		
		// initilize the vectors
		double [] driftVector = new double[monitoredFunction.getVectorSpace().getDimension()];
		double [] localStatistics = new double[monitoredFunction.getVectorSpace().getDimension()];
		double [] localEstimate = new double[monitoredFunction.getVectorSpace().getDimension()];
		double [] globalEstimate = new double[monitoredFunction.getVectorSpace().getDimension()];
		double [] slackVector = new double[monitoredFunction.getVectorSpace().getDimension()];
		
		sharedState.setDriftVector(driftVector);
		sharedState.setLocalStatisticsVector(localStatistics);
		sharedState.setLocalStatisticsSample(localEstimate);
		sharedState.setEstimateVector(globalEstimate);
		sharedState.setSlackVector(slackVector);
		
		// Create the consumer
		startUpLatch = new CountDownLatch(1);
		dataStreamConsumer = new DataStreamConsumer(session, sharedState, monitoredFunction, startUpLatch, queue);
		(new Thread(dataStreamConsumer)).start();
		
		// get the initial statistics vector and weight
		Pair<double [], Double> pair;
		try {
			pair = queue.take();
			
			sharedState.setLocalStatisticsSample(pair.getFirst());
			sharedState.setLocalStatisticsVector(pair.getFirst());
			Arrays.fill(slackVector, 0.0);
			sharedState.setSlackVector(slackVector);
			sharedState.setWeightSample(pair.getSecond());
			sharedState.setLastLocalStatisticsVectorSend(pair.getFirst());
			sharedState.setLastWeightSend(pair.getSecond());
			
			session.INIT(pair.getFirst(), pair.getSecond());
			
			// let the consumer run
			// however the consumer must not start getting values right away
			startUpLatch.countDown();
			
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * @param session The {@link OrdinaryNodeSession} object.
	 */
	public void setOrdinaryNodeSession(OrdinaryNodeSession session){
		this.session = session;
	}
	
	/**
	 * @param monitoredFunction The {@link MonitoredFunction} object.
	 */
	public void setMonitoredFunction(MonitoredFunction monitoredFunction){
		this.monitoredFunction = monitoredFunction;
	}
	
	/**
	 * @param queue The {@link BlockingQueue} object we use for getting values from the input {@link DataStream}
	 */
	public void setDataStreamQueue(BlockingQueue<Pair<double [], Double>> queue){
		this.queue = queue;
	}
	
	/**
	 * @return The {@link OrdinaryNodeSession} object.
	 */
	public OrdinaryNodeSession getOrdinaryNodeSession(){
		return this.session;
	}
	
	/**
	 * @return The {@link MonitoredFunction} object.
	 */
	public MonitoredFunction getMonitoredFunction(){
		return this.monitoredFunction;
	}
	
	/**
	 * @return The {@link BlockingQueue} object we use for getting values from the input {@link DataStream}
	 */
	public BlockingQueue<Pair<double [], Double>> getDataStreamQueue(){
		return this.queue;
	}
	
	
	/**
	 * Used by the coordinator to report to the nodes a new estimate vector.
	 * 
	 * Upon receipt of a NEW-EST message, update the estimate vector to the value specified in the message,
	 * set the value of the local statistics sample to the statistics vector sent to the coordinator, 
	 * and set the slack vector to 0. 
	 */
	public synchronized void NEW_EST(double[] estimateVector) throws RemoteException, OrdinaryNodeException {
		/**
		 * We can receive a NEW-EST message only after we have send the INIT() message or after we have
		 * send a REP() message. In the first case we are in the INIT state and in the second case we are in the UNBALANCED state
		 */
		if (sharedState.getOrdinaryNodeState() == OrdinaryNodeState.BALANCED){
			throw new OrdinaryNodeIllegalStateException();
		}
		
		// update the vectors
		sharedState.setEstimateVector(estimateVector);
		sharedState.setLocalStatisticsSample(sharedState.getLastLocalStatisticsVectorSend());
		double [] slackVector = new double[this.monitoredFunction.getVectorSpace().getDimension()];
		Arrays.fill(slackVector, 0.0);
		sharedState.setSlackVector(slackVector);
	}

	/**
	 * Used by the coordinator to report slack vector adjustments to nodes after a successful balancing process.
	 * 
	 * Upon receipt of a ADJ-SLK message, add the value specified in the message to the value of the slack vector.
	 */
	public synchronized void ADJ_SLK(double[] slackVectorAdjustment) throws RemoteException, OrdinaryNodeException {
		/**
		 * We can receive a ADJ-SLK message only after we have send a REP message to the coordinator so we must be
		 * in a UNBALANNCED state
		 */
		if (sharedState.getOrdinaryNodeState() != OrdinaryNodeState.UNBALANCED){
			throw new OrdinaryNodeIllegalStateException();
		}
		
		// update the slack vector
		sharedState.setSlackVector(VectorOps.add(sharedState.getSlackVector(), slackVectorAdjustment));
	}

	/**
	 * Used by the coordinator during the balancing process to request that a node send its statistics vector and drift vector.
	 */
	public synchronized void REQ() throws RemoteException, OrdinaryNodeException {
		/**
		 * With this implementation we do not allow concurrent balancing processes so we must have only one REQ() at a time.
		 * This means that must be in the BALANCED state now...
		 */
		if (sharedState.getOrdinaryNodeState() != OrdinaryNodeState.BALANCED){
			throw new OrdinaryNodeIllegalStateException();
		}
		
		try {
			session.REP(sharedState.getDriftVector(), sharedState.getLocalStatisticsVector(), sharedState.getWeight());
			
			// now we are in an unbalanced state
			sharedState.setOrdinaryNodeState(OrdinaryNodeState.UNBALANCED);
			
			// remember what we send to the coordinator
			sharedState.setLastLocalStatisticsVectorSend(sharedState.getLocalStatisticsVector());
			sharedState.setLastWeightSend(sharedState.getWeight());
		} catch (CoordinatorException e) {
			// this should not happen :)
		}
	}
	
	
	/**
	 * {@link DataStreamConsumer} consumes the values from the incoming input {@link DataStream}
	 * 
	 * @author Tassos Souris
	 */
	class DataStreamConsumer implements Runnable{
		private OrdinaryNodeSession session; // the session we have established
		private OrdinaryNodeSharedState sharedState; // the state we share with the remote object
		private MonitoredFunction monitoredFunction; // the monitored function for this monitoring task
		private CountDownLatch startUpLatch; // a latch to indicate that we must start
		private BlockingQueue<Pair<double [], Double>> queue; // the queue we receive from the data stream
		
		public DataStreamConsumer(OrdinaryNodeSession session, OrdinaryNodeSharedState sharedState,MonitoredFunction monitoredFunction,CountDownLatch startUpLatch, BlockingQueue<Pair<double [], Double>> queue){
			this.session = session;
			this.sharedState = sharedState;
			this.monitoredFunction = monitoredFunction;
			this.startUpLatch = startUpLatch;
			this.queue = queue;
		}
		
		public void run() {
			Pair<double [], Double> pair;
			
			try {
				// wait to be told to start
				startUpLatch.await();
				
				// start receiving values from the input stream
				while (true){
					pair = queue.take();
					
					sharedState.setLocalStatisticsVectorAndWeight(pair.getFirst(), pair.getSecond());
					
					checkLocalCondition();
				}
			} catch (InterruptedException e) {
			}		
		}
		
		/**
		 * Check local condition and if we have a constraint violation
		 */
		private void checkLocalCondition(){
			/*
			 * Compute statistics delta vector
			 * 
			 * deltaV = (weight*localStatistics - 
			 *   weightEstimate*localEstimate - 
			 *   (weight - weightEstimate)*globalEstimate) / 
			 *      weight 
			 */
			double ratioW = sharedState.getWeightSample()/sharedState.getWeight();
			double[] deltaV = VectorOps.multAndAdd(sharedState.getLocalStatisticsVector(), 
					-ratioW, sharedState.getLocalStatisticsSample());
			VectorOps.multAndAddTo(deltaV,ratioW-1.0, sharedState.getEstimateVector());
			
			/*
			 * Update drift vector
			 * 
			 * driftVector = globalEstimate + deltaV + slackVector/weight
			 */
			double [] driftVector = new double[monitoredFunction.getVectorSpace().getDimension()];
			
			VectorOps.cpy(driftVector, sharedState.getEstimateVector());
			VectorOps.addTo(driftVector, deltaV);
			VectorOps.multAndAddTo(driftVector, 1.0/sharedState.getWeight(), sharedState.getSlackVector());
			
			sharedState.setDriftVector(driftVector);
			
			
			if (!monitoredFunction.getIsMonochromaticSpecification().IsSatisfiedBy(new Ball(sharedState.getEstimateVector(),driftVector))){
				sharedState.setOrdinaryNodeState(OrdinaryNodeState.UNBALANCED);
				// report to coordinator
				try {
					session.REP(driftVector, sharedState.getLocalStatisticsVector(), sharedState.getWeight());
				} catch (RemoteException e) {
				} catch (CoordinatorException e) {
				}
			}
		}
	}
	
	
	
	/**
	 * {@link OrdinaryNodeSharedState} is a monitor object to share data between the {@link OrdinaryNode} remote object 
	 * and the {@link DataStreamConsumer}.
	 * 
	 * @author Tassos Souris
	 */
	class OrdinaryNodeSharedState{
		private double [] localStatisticsVector;
		private double weight;
		private double [] estimateVector;
		private double [] statisticsDeltaVector;
		private double [] driftVector;
		private double [] slackVector;
		private double [] localStatisticsSample;
		private double weigthSample;
		
		private double [] lastLocalStatisticsVectorSend;
		private double lastWeightSend;
		
		private OrdinaryNodeState state;
		
		
		public OrdinaryNodeSharedState(){
			
		}
		
		/**
		 * @param The local statistics vector
		 */
		public synchronized void setLocalStatisticsVector(double [] localStatisticsVector){
			 this.localStatisticsVector = localStatisticsVector;
		}
		
		/**
		 * @param The weight assigned to the stream
		 */
		public synchronized void setWeight(double weight){
			this.weight = weight;
		}
		
		/**
		 * @param The estimate vector
		 */
		public synchronized void setEstimateVector(double [] estimateVector){
			this.estimateVector = estimateVector;
		}
		
		/**
		 * @param The statistics delta vector
		 */
		public synchronized void setStatisticsDeltaVector(double [] statisticsDeltaVector){
			this.statisticsDeltaVector = statisticsDeltaVector;
		}
		
		/**
		 * @param The drift vector
		 */
		public synchronized void setDriftVector(double [] driftVector){
			this.driftVector = driftVector;
		}
		
		/**
		 * @param The slack vector
		 */
		public synchronized void setSlackVector( double [] slackVector){
			this.slackVector = slackVector;
		}
		
		/**
		 * @param The local statistics sample
		 */
		public synchronized void setLocalStatisticsSample(double [] localStatisticsSample){
			this.localStatisticsSample = localStatisticsSample;
		}
		
		/**
		 * @param The weight sample
		 */
		public synchronized void setWeightSample(double weightSample){
			this.weigthSample = weightSample;
		}
		
		/**
		 * @param lastLocalStatisticsVectorSend
		 */
		public synchronized void setLastLocalStatisticsVectorSend(double [] lastLocalStatisticsVectorSend){
			this.lastLocalStatisticsVectorSend = lastLocalStatisticsVectorSend;
		}
		
		/**
		 * @param weight
		 */
		public synchronized void setLastWeightSend(double weight){
			this.weight = weight;
		}
		
		/**
		 * @param state The state.
		 */
		public synchronized void setOrdinaryNodeState(OrdinaryNodeState state){
			this.state = state;
		}
		
		/**
		 * @param localStatisticsVector
		 * @param weight
		 */
		public synchronized void setLocalStatisticsVectorAndWeight(double [] localStatisticsVector, double weight){
			this.localStatisticsVector = localStatisticsVector;
			this.weight = weight;
		}
		
		/**
		 * @return The local statistics vector
		 */
		public synchronized double [] getLocalStatisticsVector(){
			return this.localStatisticsVector;
		}
		
		/**
		 * @return The weight assigned to the stream
		 */
		public synchronized double getWeight(){
			return this.weight;
		}
		
		/**
		 * @return The estimate vector
		 */
		public synchronized double [] getEstimateVector(){
			return this.estimateVector;
		}
		
		/**
		 * @return The statistics delta vector
		 */
		public synchronized double [] getStatisticsDeltaVector(){
			return this.statisticsDeltaVector;
		}
		
		/**
		 * @return The drift vector
		 */
		public synchronized double [] getDriftVector(){
			return this.driftVector;
		}
		
		/**
		 * @return The slack vector
		 */
		public synchronized double [] getSlackVector(){
			return this.slackVector;
		}
		
		/**
		 * @return The local statistics sample
		 */
		public synchronized double [] getLocalStatisticsSample(){
			return this.localStatisticsSample;
		}
		
		/**
		 * @return The weight sample
		 */
		public synchronized double getWeightSample(){
			return this.weigthSample;
		}
		
		/**
		 * @return The lastLocalStatisticsVectorSend
		 */
		public synchronized double [] getLastLocalStatisticsVectorSend(){
			return this.lastLocalStatisticsVectorSend;
		}
		
		/**
		 * @return The lastWeightSend
		 */
		public synchronized double getLastWeightSend(){
			return this.lastWeightSend;
		}
		
		/**
		 * @return The state.
		 */
		public synchronized OrdinaryNodeState getOrdinaryNodeState(){
			return this.state;
		}
	}
}