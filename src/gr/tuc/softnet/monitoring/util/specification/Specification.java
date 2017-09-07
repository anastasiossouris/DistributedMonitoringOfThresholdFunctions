package gr.tuc.softnet.monitoring.util.specification;

/**{@link Specification} is the interface to the implementation of the Specification pattern. 
 * See http://en.wikipedia.org/wiki/Specification_pattern
 * 
 * @author Tassos Souris
 */
public interface Specification<T> {
	public boolean IsSatisfiedBy(T entity);
	
	public Specification<T> And(Specification<T> other);
	public Specification<T> Or(Specification<T> other);
	public Specification<T> Not();
}
