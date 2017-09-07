package gr.tuc.softnet.monitoring.example;

import gr.tuc.softnet.monitoring.protocol.constraint.Ball;
import gr.tuc.softnet.monitoring.protocol.constraint.IsMonochromaticSpecification;
import gr.tuc.softnet.monitoring.util.specification.Specification;

public class OneDimThresholdIsMonochromaticSpecification implements IsMonochromaticSpecification{

	public boolean IsSatisfiedBy(Ball entity) {
		double [] reference = entity.getEstimateVector();
		double [] drift = entity.getDriftVector();
		
		return reference[0]>=0.0 && drift[0]>=0.0;
	}

	public Specification<Ball> And(Specification<Ball> other) {
		return null;
	}

	public Specification<Ball> Or(Specification<Ball> other) {
		return null;
	}

	public Specification<Ball> Not() {
		return null;
	}

}
