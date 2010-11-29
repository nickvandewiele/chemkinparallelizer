package parameter_estimation;

import java.io.PrintWriter;
/**
 * algebra operations, like gram-schmidt orthonormalisation
 * @author nmvdewie
 *
 */
public class Algebra {

	/**
	* If successes have been found in multiple directions, this means that a new basis should be chosen<BR>
	* Basis vectors should be chosen in the joint direction of the successes<BR>
	* This method sets a new basis, but the actual implementation is still a mystery to me<BR>
	 * @param dd
	 * @param bbasis
	*/
	public static double[][] setNewBasis (double[] dd, double[][] bbasis){
		
	for (int i = 0; i < dd.length; i++) {
		for (int j = 0; j < dd.length; j++) {
			bbasis[j][i] = dd[i] * bbasis[j][i];
		}
		for (int k = i+1; k < dd.length; k++) {
			for (int j = 0; j < dd.length; j++) {
				bbasis[j][i] = bbasis[j][i] + dd[k] * bbasis[j][k];	
			}
		}
	}
	return bbasis;
	}

	/**
	* 
	* @param bbasis
	* @return An orthonormal basis is derived and returned from the matrix basis using the Gram-Schmidt algorithm
	*/
	public static double[][] gramschmidt (double[][] basis, double[][] bbasis) {
	//  gram schmidt orthonormalization
	
	for(int  i = 0; i < bbasis[0].length; i++){
		for(int k = 0; k < i-1; k++){
			double scal_prod = 0.0;
			for (int j = 0; j < bbasis[0].length; j++){
				scal_prod = scal_prod + bbasis[j][i] * bbasis[j][k];
			}
			for (int j = 0; j < bbasis[0].length; j++) {
				bbasis[j][i] = bbasis[j][i] - scal_prod * bbasis[j][k];
			}
		}
	// calculation of norms of every basis vector: 
		double norm = 0.0;
		for (int j = 0; j < basis[0].length; j++){
			norm = norm + basis[j][i] * basis[j][i];
		}
	// normalization of new bases:          
		for (int j = 0; j < basis[0].length; j++){
			basis[j][i] = basis[j][i] / Math.sqrt(norm);
		}
	}
	//  nit = nit+1;
	     
	return bbasis;
	}

	public static double [] resetD (double[] dd){
		for (int i = 0; i < dd.length; i++) {
			dd[i] = 0;
		}
		return dd;
	}

	/**
	 * Initialization of the basis, taking unit vectors in every direction of the parameters
	
	 * @param bbasis
	 */
	public static double[][] basisInit (double[][] bbasis) {
	for (int i = 0; i < bbasis[0].length; i++){
		for (int j = 0; j < bbasis[0].length; j++) {
			bbasis[i][j] = 0.0;
		}
		bbasis[i][i] = 1.0;
	}
	return bbasis;
	}

	public static double checkLowerUpperBounds(double d, double lower, double upper, PrintWriter out){
		double dummy = d; 
		if (d < lower) {
			out.println("New parameter guess has exceeded user-defined lower limits!");
			out.println("new guesses will be equal to user-defined lower limits");
			dummy = lower;
		}
		else if (d > upper) {
			out.println("New parameter guess has exceeded user-defined upper limits!");
			out.println("new guesses will be equal to user-defined upper limits");
			dummy = upper;
		}
		else {
			//do nothing
		}
		return dummy;
	}

}
