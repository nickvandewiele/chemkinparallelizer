package stat;
import java.io.IOException;

import optimization.Algebra;
import optimization.Function;
import optimization.Optimization;

import org.apache.log4j.Logger;

import parsers.ConfigurationInput;

import applications.ParameterEstimationDriver;
import cern.jet.stat.Probability;

public class Statistics {
	static Logger logger = Logger.getLogger(ParameterEstimationDriver.logger.getName());
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
	private int noParams;
	private int noExperiments;
	private int noResp; 
	
public Statistics(Optimization optimization){
	this.optimization = optimization;
	noParams = ConfigurationInput.chemistry.getParams().getNoFittedParameters();
	noExperiments = ConfigurationInput.experiments.total_no_experiments;
	noResp = ConfigurationInput.experiments.experimentalValues.length;
}

/**
 * variance-covariance matrix of parameter estimations
 * @throws IOException 
 */
private void calcVarCovar() throws IOException, InterruptedException{
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
	JTJminus1 = Algebra.gaussj(JTJminus1, JTJminus1.length); //inverse matrix of JTJ	
	var_covar = JTJminus1;
	
}
private void calcCorr(){
	corr = new double[var_covar.length][var_covar[0].length];
	for (int k=0; k < noParams; k++)      // calculate curvature matrix JTJ
        for (int j=0; j < noParams; j++)
        {
        	corr[k][j] = var_covar[k][j] / Math.sqrt(var_covar[k][k]*var_covar[j][j]);
        }
}
private void calcANOVA() throws Exception{
	Function function = new Function (ConfigurationInput.experiments.experimentalValues,
			optimization.testNewParameters(optimization.buildFullParamVector(optimization.retrieveFittedParameters()),true));
	SREG = function.getSREG();
	SRES = function.getSRES();
	Fvalue = (SREG/noParams) / (SRES/(noExperiments * noResp - noParams));
}
/**
 * t-value for significance of individual parameter estimation with respect to zero
 */
private void calcTValues(){
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
private void calcConfIntervals() throws IOException, InterruptedException{
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
private void calc_tabulated_t(){
	//TODO if 2nd argument == 0, exception is returned!
	tabulated_t_value = Probability.studentTInverse(alpha, noExperiments*noResp - noParams);
}
/**
 * @category getter
 * @return
 * @throws IOException
 * @throws InterruptedException
 */
public double [][] get_Var_Covar() throws IOException, InterruptedException{
	calcVarCovar();
	return var_covar;
}
/**
 * @category getter
 * @return
 * @throws IOException
 * @throws InterruptedException
 */
public double [][] get_Corr() throws IOException, InterruptedException{
	calcCorr();
	return corr;
}
/**
 * @category getter
 * @return
 * @throws IOException
 * @throws InterruptedException
 */
public double[] getT_values() {
	calcTValues();
	return t_values;
}
/**
 * @category getter
 * @return
 * @throws IOException
 * @throws InterruptedException
 */
public double[][] getJTJ() {
	return JTJ;
}
/**
 * @category getter
 * @return
 * @throws IOException
 * @throws InterruptedException
 */
public double getTabulated_t_value() {
	calc_tabulated_t();
	return tabulated_t_value;
}
/**
 * @category getter
 * @return
 * @throws IOException
 * @throws InterruptedException
 */
public double[][] getConfIntervals() throws IOException, InterruptedException {
	calcConfIntervals();
	return confidence_intervals;
}
/**
 * @category getter
 * @return
 * @throws IOException
 * @throws InterruptedException
 */
public double getSREG() throws Exception {
	calcANOVA();
	return SREG;
}
/**
 * @category getter
 * @return
 * @throws IOException
 * @throws InterruptedException
 */
public double getSRES() throws Exception {
	calcANOVA();
	return SRES;
}
/**
 * @category getter
 * @return
 * @throws IOException
 * @throws InterruptedException
 */
public double getFvalue() throws Exception {
	calcANOVA();
	return Fvalue;
}
/*
public double getTabulated_F_value() {
	calc_tabulated_F();
	return tabulated_F_value;
}
*/


}
