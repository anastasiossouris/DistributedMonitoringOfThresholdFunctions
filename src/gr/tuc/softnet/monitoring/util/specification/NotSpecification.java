package gr.tuc.softnet.monitoring.util.specification;

/**
 * See http://en.wikipedia.org/wiki/Specification_pattern
 * 
 * @author Tassos Souris
 */
public class NotSpecification<T> extends CompositeSpecification<T> {
	private Specification<T> wrapped;
	
	public NotSpecification(Specification<T> wrapped){
		this.wrapped = wrapped;
	}
	
	@Override
	public boolean IsSatisfiedBy(T candidate) {
		return !wrapped.IsSatisfiedBy(candidate);
	}

}
