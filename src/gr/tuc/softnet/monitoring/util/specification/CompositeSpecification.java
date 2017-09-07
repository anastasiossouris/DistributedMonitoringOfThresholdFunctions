package gr.tuc.softnet.monitoring.util.specification;

/**
 * See http://en.wikipedia.org/wiki/Specification_pattern
 * 
 * @author Tassos Souris
 */
public abstract class CompositeSpecification<T> implements Specification<T> {
	public abstract boolean IsSatisfiedBy(T candidate);
	 
    public Specification<T> And(Specification<T> other)
    {
        return new AndSpecification<T>(this, other);
    }

    public Specification<T> Or(Specification<T> other)
    {
        return new OrSpecification<T>(this, other);
    }

    public Specification<T> Not()
    {
       return new NotSpecification<T>(this);
    }

}
