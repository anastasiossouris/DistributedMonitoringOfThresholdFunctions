package gr.tuc.softnet.monitoring.util.vector;


/**
 * Vector operations.
 * 
 * This class contains static public methods implementing some basic arithmetic 
 * operations on double arrays (vectors).
 * 
 * 
 * @author vsam
 *
 */
public class VectorOps {
		
	/**
	 * Add operand to target
	 * @param target
	 * @param operand
	 */
	static public void addTo(double[] target, double[] operand) {
		assert target.length == operand.length;
		for(int i=0;i<target.length;i++)
			target[i] += operand[i];
	}

	/**
	 * Return a+b
	 * @param a
	 * @param b
	 * @return
	 */
	static public double[] add(double[] a, double[] b) {
		double[] ret = a.clone();
		addTo(ret,b);
		return ret;
	}
	
	/**
	 * Subtract operand from target
	 * @param target
	 * @param operand
	 * @return
	 */
	static public double[] subFrom(double[] target, double[] operand) {
		assert target.length == operand.length;
		for(int i=0;i<target.length;i++)
			target[i] -= operand[i];
		return target;
	}
	
	/**
	 * Return a-b
	 * @param a
	 * @param b
	 * @return
	 */
	static public double[] sub(double[] a, double[] b) {
		return subFrom(a.clone(), b);
	}
	
	/**
	 * Multiply target by a
	 * @param target
	 * @param a
	 */
	static public void multBy(double[] target, double a) {
		for(int i=0;i<target.length;i++)
			target[i] *= a;		
	}
	
	/**
	 * Return a*b
	 * @param a
	 * @param b
	 * @return
	 */
	static public double[] mult(double[] a, double b) {
		double[] ret = a.clone();
		multBy(ret,b);
		return ret;
	}
	
	/**
	 * Copy the elements of src to the elements of dst
	 * @param dest
	 * @param src
	 */
	static public void cpy(double[] dest, double[] src) {
		assert dest.length == src.length;
		for(int i=0;i<dest.length; i++) {
			dest[i] = src[i];
		}
	}
	
	
	/**
	 * Add p*b to a.
	 * 
	 * @param a accumulator vector
	 * @param p scalar factor
	 * @param b vector operand
	 */
	static public void multAndAddTo(double[] a, double p, double[] b) {
		assert a.length==b.length;
		for(int i=0;i<a.length;i++) {
			a[i] += p*b[i];
		}
	}
	
	/**
	 * Return a+p*b
	 */
	static public double[] multAndAdd(double[] a, double p, double[] b) {
		double[] ret = a.clone();
		multAndAddTo(ret, p, b);
		return ret;
	}
}
