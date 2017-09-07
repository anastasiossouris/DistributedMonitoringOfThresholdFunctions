package gr.tuc.softnet.monitoring.protocol.constraint;

import gr.tuc.softnet.monitoring.util.specification.Specification;

/**
 * According to the protocol the local constraint each node maintains is to check whether the ball B(e(t),u(t)) is monochromatic, meaning whether the vectors
 * contained in the ball have the same color. 
 * This constraint is modeled with a {@link Specification} over a Ball object.
 * 
 * @author Tassos Souris
 */
public interface IsMonochromaticSpecification extends Specification<Ball>{

}
