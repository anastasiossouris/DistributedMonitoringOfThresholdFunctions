package gr.tuc.softnet.monitoring.protocol.constraint;

/**
 * Wrapper around the dimension of the vectors.
 * 
 * @author Tassos Souris
 */
public class VectorSpace {
	private int dimension;
	
	public VectorSpace(){
		this.dimension = 0;
	}
	
	public VectorSpace(int dimension){
		this.dimension = dimension;
	}
	
	/**
	 * @param dimension The dimension of the vector space.
	 */
	public void setDimension(int dimension){
		this.dimension = dimension;
	}
	
	/**
	 * @return The dimension of the vector space.
	 */
	public int getDimension(){
		return this.dimension;
	}
}
