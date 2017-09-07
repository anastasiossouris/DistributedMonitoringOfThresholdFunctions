package gr.tuc.softnet.monitoring.util.vector;

import java.util.Arrays;
import java.util.List;

/**
 * {@link VectorCalculations} provides static functions used for the calculations of various vectors needed by the protocol.
 * 
 * @author Tassos Souris
 */
public class VectorCalculations {
	
	/**
	 * Calculate the adjustment to the slack vector.
	 */
	public static void slackVectorAdjustment(double [] slackVectorAdj, double [] balancedVector, double weight, double [] driftVector){
		slackVectorAdj = VectorOps.sub(balancedVector, driftVector);
		VectorOps.multBy(slackVectorAdj, weight);
	}

	/**
	 * Calculate the estimate vector.
	 */
	public static void estimateVector(double [] estimateVector, List<Double> weights, List<double []> localStatisticsSamples){
		/*
		 * globalEstimate = SUM( nodeWeight[node] * localEstimate[node] 
		 *    for node in nodes ) /
		 *  SUM( nodeWeight[node]  for node in nodes ) 
		 */
		assert weights.size() == localStatisticsSamples.size();
		
		Arrays.fill(estimateVector, 0.0);
		double sumw = 0.0;
		
		for ( int i = 0, size = weights.size(); i < size; ++i ){
			double w = weights.get(i);
			
			sumw += w;
			
			VectorOps.addTo(estimateVector, 
					VectorOps.mult(localStatisticsSamples.get(i), w));
		}
		
		VectorOps.multBy(estimateVector, 1.0/sumw);
	}
	
	/**
	 * Calculate the drift vector.
	 */
	public static void balancedVector(double [] balancedVector, List<Double> weights, List<double []> drifts){
		/*
		 * Compute b
		 * 
		 * b = SUM( nodeWeight[node]*nodeDrift[node] 
		 *   for node in balancing_group)  / 
		 *   SUM( nodeWeight[node] for node in balancing_group) 
		 */
		assert weights.size() == drifts.size();
		
		double W = 0.0;
		Arrays.fill(balancedVector, 0.0);
		for( int i = 0, size = weights.size(); i < size; ++i ) {
			double w = weights.get(i);
			VectorOps.multAndAddTo(balancedVector, w, drifts.get(i));
			W += w;
		}
		VectorOps.multBy(balancedVector, 1.0/W);
	}
}
