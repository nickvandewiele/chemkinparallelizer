package stat;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import parameter_estimation.*;
import cern.jet.stat.Probability;

public class Statistics {
	static Logger logger = Logger.getLogger(ParameterEstimationDriver.logger.getName());
	private int noExperiments;
	private int noParams;
	private int noResp;
	private double [][] var_covar; //variance-covariance matrix of parameter estimations
	private double [][] corr; //correlation matrix of parameter estimations
	private double [][] JTJ;//curvature matrix, transpose(Jacobian).Jacobian
	private double [][] JTJminus1;//inverse of curvature matrix, equal to covariance matrix of parameter estimations
	private double [] t_values; //array of t_values of individual significance of fitted parameters
	private double alpha = 0.05; //uncertainty used in tests of significance of parameter estimations
	
	private Optimization optimization;
	private double SREG;
	private double SRES;
	private double Fvalue;
	private double tabulated_t_value;//t_(1-alpha/2)
	private double tabulated_F_value;
	private double[][] confidence_intervals; 
	
public Statistics(Optimization optimization){
	this.optimization = optimization;
	noExperiments = optimization.getNBMTHost().getNPTS();
	noParams = optimization.getNBMTHost().getNPARMS();
	noResp = optimization.getNBMTHost().getNRESP();
}

/**
 * variance-covariance matrix of parameter estimations
 * @throws IOException 
 */
public void calcVarCovar() throws IOException, InterruptedException{
	//double [][][] J = optimization.getNBMTmultiDHost().dGetFullJac();
	double [][] J = optimization.getNBMTHost().dGetFullJac();
	
	
	var_covar = new double[noParams][noParams];
	
	JTJ = new double[noParams][noParams];
	
	for (int k=0; k < noParams; k++)      // calculate curvature matrix JTJ
        for (int j=0; j < noParams; j++)
        {
            JTJ[j][k] = 0.0;
            for (int i=0; i < noExperiments * noResp; i++)
          		  JTJ[j][k] += J[i][j] * J[i][k];	           	             
        }
	
	JTJminus1 = JTJ;//prevents gaussj from overwriting JTJ
	JTJminus1 = gaussj(JTJminus1, JTJminus1.length); //inverse matrix of JTJ
	
	
/*	double s_squared = sos/(no_experiments - no_parameters);
	
	for (int i = 0; i < JTJminus1.length; i++) {
		for (int k = 0; k < JTJminus1[0].length; k++) {
			var_covar[i][k] = JTJminus1[i][k] * s_squared;  
		}
	}
*/	
	var_covar = JTJminus1;
	
}
public void calcCorr(){
	corr = new double[var_covar.length][var_covar[0].length];
	for (int k=0; k < noParams; k++)      // calculate curvature matrix JTJ
        for (int j=0; j < noParams; j++)
        {
        	corr[k][j] = var_covar[k][j] / Math.sqrt(var_covar[k][k]*var_covar[j][j]);
        }
}
public void calcANOVA() throws IOException, InterruptedException{
	Function function = new Function (optimization.getModelValues(optimization.buildFullParamVector(optimization.retrieve_fitted_parameters()),true), optimization.getExp());
	SREG = function.getSREG();
	SRES = function.getSRES();
	Fvalue = (SREG/noParams) / (SRES/(noExperiments * noResp - noParams));
}
/**
 * t-value for significance of individual parameter estimation with respect to zero
 */
public void calcTValues(){
	//double [] params = optimization.getNBMTmultiDHost().getParms();
	double [] params = optimization.getNBMTHost().getParms();
	
	t_values = new double[params.length];
	for (int i = 0; i < t_values.length; i++) {
		t_values[i] = params[i] / Math.sqrt(var_covar[i][i]);
	}
	
}

/**
 * 95% confidence intervals
 * @throws IOException 
 */
public void calcConfIntervals() throws IOException, InterruptedException{
	//double [] params = optimization.getNBMTmultiDHost().getParms();
	double [] params = optimization.getNBMTHost().getParms();
	confidence_intervals = new double[noParams][3];
	
	calc_tabulated_t();
	calcVarCovar();
	
	for (int i = 0; i < confidence_intervals.length; i++) {
		confidence_intervals[i][0] = params[i];//parameter estimation is added in first column
		confidence_intervals[i][1] = params[i]+getTabulated_t_value()*Math.sqrt(get_Var_Covar()[i][i]);//upper limit
		confidence_intervals[i][2] = params[i]-getTabulated_t_value()*Math.sqrt(get_Var_Covar()[i][i]);//lower limit	
	}
}
/**
 * tabulated two-sided t-value for n*v-p experiments for an accumulated probability of 1-alpha/2
 */
public void calc_tabulated_t(){
	tabulated_t_value = Probability.studentTInverse(alpha, noExperiments*noResp - noParams);
}
/*
public void calc_tabulated_F(){
	tabulated_F_value = (Probability.chiSquare(no_parameters, 1-alpha)/no_parameters) / (Probability.chiSquare(no_experiments * no_responses - no_parameters, 1-alpha)/(no_experiments * no_responses - no_parameters));
}
*/
public double[][] gaussj( double[][] a, int N )
// Inverts the double array a[N][N] by Gauss-Jordan method
// M.Lampton UCB SSL (c)2003, 2005
{
    double det = 1.0, big, save;
    int i,j,k,L;
    int[] ik = new int[100];
    int[] jk = new int[100];
    for (k=0; k<N; k++)
    {
        big = 0.0;
        for (i=k; i<N; i++)
          for (j=k; j<N; j++)          // find biggest element
            if (Math.abs(big) <= Math.abs(a[i][j]))
            {
                big = a[i][j];
                ik[k] = i;
                jk[k] = j;
            }
        //if (big == 0.0) return 0.0;
        if (big == 0.0) return null;
        i = ik[k];
        if (i>k)
          for (j=0; j<N; j++)          // exchange rows
          {
              save = a[k][j];
              a[k][j] = a[i][j];
              a[i][j] = -save;
          }
        j = jk[k];
        if (j>k)
          for (i=0; i<N; i++)
          {
              save = a[i][k];
              a[i][k] = a[i][j];
              a[i][j] = -save;
          }
        for (i=0; i<N; i++)            // build the inverse
          if (i != k)
            a[i][k] = -a[i][k]/big;
        for (i=0; i<N; i++)
          for (j=0; j<N; j++)
            if ((i != k) && (j != k))
              a[i][j] += a[i][k]*a[k][j];
        for (j=0; j<N; j++)
          if (j != k)
            a[k][j] /= big;
        a[k][k] = 1.0/big;
        det *= big;                    // bomb point
    }                                  // end k loop
    for (L=0; L<N; L++)
    {
        k = N-L-1;
        j = ik[k];
        if (j>k)
          for (i=0; i<N; i++)
          {
              save = a[i][k];
              a[i][k] = -a[i][j];
              a[i][j] = save;
          }
        i = jk[k];
        if (i>k)
          for (j=0; j<N; j++)
          {
              save = a[k][j];
              a[k][j] = -a[i][j];
              a[i][j] = save;
          }
    }
    //return det;
    return a;
}
public double [][] get_Var_Covar() throws IOException, InterruptedException{
	calcVarCovar();
	return var_covar;
}
public double [][] get_Corr() throws IOException, InterruptedException{
	calcCorr();
	return corr;
}
public void printArray(double [] d, PrintWriter out){
	for (int i = 0; i < d.length; i++) {
		out.print(d[i]+" ");
		logger.info(d[i]+" ");
	}
	out.println();
	//System.out.println();
}
public void printMatrix(double [][] d,PrintWriter out){
	for (int i = 0; i < d.length; i++) {
		for (int j = 0; j < d[0].length; j++) {
			out.print(d[i][j]+" ");
			logger.info(d[i][j]+" ");			
		}
		out.println();
    	logger.info("");
	}
	out.println();
	//System.out.println();
}
public void print3DMatrix(double [][][] d,PrintWriter out){
	for (int i = 0; i < d.length; i++) {
		for (int j = 0; j < d[0].length; j++) {
			for (int k = 0; k < d[0][0].length; k++) {
				out.print(d[i][j][k]+" ");
				logger.info(d[i][j][k]+" ");			
			}
			out.println();
	    	logger.info("");
		}
		out.println();
    	logger.info("");
	}
	out.println();
	//System.out.println();
}
public double[] getT_values() {
	calcTValues();
	return t_values;
}
public double[][] getJTJ() {
	return JTJ;
}
public double getTabulated_t_value() {
	calc_tabulated_t();
	return tabulated_t_value;
}
public double[][] getConfIntervals() throws IOException, InterruptedException {
	calcConfIntervals();
	return confidence_intervals;
}

public double getSREG() throws IOException, InterruptedException {
	calcANOVA();
	return SREG;
}

public double getSRES() throws IOException, InterruptedException {
	calcANOVA();
	return SRES;
}

public double getFvalue() throws IOException, InterruptedException {
	calcANOVA();
	return Fvalue;
}
/*
public double getTabulated_F_value() {
	calc_tabulated_F();
	return tabulated_F_value;
}
*/

public int getNoExperiments() {
	return noExperiments;
}


public int getNoParams() {
	return noParams;
}

public int getNoResp() {
	return noResp;
}
}
