package gr.tuc.softnet.monitoring.example;

import gr.tuc.softnet.monitoring.protocol.constraint.IsMonochromaticSpecification;
import gr.tuc.softnet.monitoring.protocol.constraint.IsSafeSpecification;
import gr.tuc.softnet.monitoring.protocol.constraint.MonitoredFunction;
import gr.tuc.softnet.monitoring.protocol.constraint.VectorSpace;

public class OneDimThreshold implements MonitoredFunction{
	private VectorSpace vectorSpace = null;
	private IsMonochromaticSpecification specification = null;
			
	public OneDimThreshold(){
		vectorSpace = new VectorSpace();
		vectorSpace.setDimension(1);
		specification = new OneDimThresholdIsMonochromaticSpecification();
	}
	
	public VectorSpace getVectorSpace() {
		return vectorSpace;
	}

	public IsMonochromaticSpecification getIsMonochromaticSpecification() {
		return specification;
	}

	public IsSafeSpecification getIsSafeSpecification() {
		return null;
	}

}
