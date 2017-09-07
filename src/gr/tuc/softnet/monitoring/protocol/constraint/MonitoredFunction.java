package gr.tuc.softnet.monitoring.protocol.constraint;

/**
 * {@link MonitoredFunction} represents the monitored function used for the protocol.
 * 
 * @author Tassos Souris
 */
public interface MonitoredFunction {
	/**
	 * @return The {@link VectorSpace} for the monitored function.
	 */
	VectorSpace getVectorSpace();
	
	/**
	 * @return The {@link IsMonochromaticSpecification} for the monitored function.
	 */
	IsMonochromaticSpecification getIsMonochromaticSpecification();
	
	/**
	 * @return The {@link IsSafeSpecification} for the monitored function.
	 */
	IsSafeSpecification getIsSafeSpecification();
}
