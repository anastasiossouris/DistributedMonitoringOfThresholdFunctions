package gr.tuc.softnet.monitoring.util.specification;

/**
 * See http://en.wikipedia.org/wiki/Specification_pattern
 * 
 * @author Tassos Souris
 */
public class OrSpecification<T> extends CompositeSpecification<T>{
	private Specification<T> one;
	private Specification<T> other;
	
	public OrSpecification(Specification<T> one, Specification<T> other){
		this.one = one;
		this.other = other;
	}
	
	@Override
	public boolean IsSatisfiedBy(T candidate) {
		return one.IsSatisfiedBy(candidate) || other.IsSatisfiedBy(candidate);
	}

}
