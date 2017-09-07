package gr.tuc.softnet.monitoring.protocol.constraint;

/**
 * {@link Ball} represents the ball constructed as part of the protocol. 
 * We are interested for now only for the estimate vector and the drift vector that define the
 * ball.
 * 
 * @author Tassos Souris
 */
public class Ball {
	private double [] estimateVector;
	private double [] driftVector;
	
	public Ball(){
		
	}
	
	public Ball(double [] estimateVector, double [] driftVector){
		this.estimateVector = estimateVector;
		this.driftVector = driftVector;
	}
	
	/**
	 * @param estimateVector The estimateVector for the Ball.
	 */
	public void setEstimateVector(double [] estimateVector){
		this.estimateVector = estimateVector;
	}
	
	/**
	 * @param driftVector The driftVector for the Ball.
	 */
	public void setDriftVector(double [] driftVector){
		this.driftVector = driftVector;
	}
	
	/**
	 * @return The estimate vector of the Ball.
	 */
	public double [] getEstimateVector(){
		return this.estimateVector;
	}
	
	/**
	 * @return The drift vector of the ball.
	 */
	public double [] getDriftVector(){
		return this.driftVector;
	}
}
