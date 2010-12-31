package tests;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import parameter_estimation.Algebra;
	
public class AlgebraTest {
	double[][] matrix;
	double[][] normalizedMatrix;
	
	@Before
	public void setUp() throws Exception {
		matrix = new double[2][2];
		matrix[0][0] = 0;
		matrix[0][1] = 1;
		matrix[1][0] = 2;
		matrix[1][1] = 1;
		normalizedMatrix = new double[2][2];
		normalizedMatrix[0][0] = 0;
		normalizedMatrix[0][1] = 1;
		normalizedMatrix[1][0] = 1;
		normalizedMatrix[1][1] = 0;

	}

	@After
	public void tearDown() throws Exception {
		matrix = null;
		normalizedMatrix = null;
	}

	@Test
	public void testGramschmidt() {
		double [][] test = Algebra.gramschmidt(matrix);
		for(int i = 0; i < normalizedMatrix.length; i++){
			for(int j = 0; j < normalizedMatrix[0].length; j++){
				Assert.assertEquals(test[i][j],normalizedMatrix[i][j]);
			}
		}
	}
}
