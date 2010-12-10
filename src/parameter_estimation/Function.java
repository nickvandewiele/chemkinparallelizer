package parameter_estimation;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;


public class Function {
	static Logger logger = Logger.getLogger(ParameterEstimationDriver.logger.getName());
	private Experiments experiments;
	private ModelValues modelValues;
	//TODO resid could also be modeled as having two children: Effluent, Ignition Delay
	private Double [] residEffluent;
	private Double [] residIgnition;
	private double [] resid;
	
	public double[] getResid() {
		computeResid();
		return resid;
	}
	/**
	 * creates double [] resid which is a concatenation of the Effluent Resid and the Ignition Resid
	 */
	private void computeResid() {
		int dummy = 0;
		if((experiments.getNoRegularExperiments()!=0)||experiments.getResponseVariables().size()!=0){
			computeEffluentResid();
			dummy += residEffluent.length;
		}
		if(experiments.getNoIgnitionDelayExperiments()!=0){
			computeIgnitionResid();
			dummy += residIgnition.length;
		}
	
		resid = new double[dummy];
		int counter = 0;
		
		/**
		 * TODO checking residEffluent/residIgnition should be less patchy!
		 */
		if(!(residEffluent == null)){
			for(int i = 0; i < residEffluent.length; i++){
				resid[counter] = residEffluent[i];
				counter++;
			}	
		}
		if(!(residIgnition == null)){
			for(int i = 0; i < residIgnition.length; i++){
				resid[counter] = residIgnition[i];
				counter++;
			}   
		}
		                
		return;
	}


	private Map<String,Double> covarianceEffluent = new HashMap<String,Double>();
	public double functionValue;
	//boolean weighted_regression determines whether a weighted or an unweighted regression will be applied.
	private boolean weightedRegression = true;

	//TODO most of the methods here should be in Statistics...
	public Function (Experiments experiments, ModelValues modelValues){
		this.experiments = experiments;
		this.modelValues = modelValues;
		if(experiments.getNoRegularExperiments()!=0){
			residEffluent = new Double[experiments.getNoRegularExperiments()*experiments.getResponseVariables().size()];	
		}
		if(experiments.getNoIgnitionDelayExperiments()!=0){
			residIgnition = new Double[experiments.getNoIgnitionDelayExperiments()];	
		}
		
		//species_names = new String[e.get(0).size()];
	}

	/**
	 * returns the sum of the regression (model) values
	 * 
	 * @return
	 */
	public double getSREG(){
		double SREG = 0.0;
		
		//Effluent part:
		Map<String,Double> average = null;
		if (weightedRegression) average = experiments.getExperimentalValues().calcExperimentalEffluentAverage();
		LinkedList<Map<String,Double>> modelEffluent = modelValues.getModelEffluentValues();
		LinkedList<String> speciesNames = experiments.getResponseVariables();
		for(int i=0;i<modelEffluent.size();i++)//Loop over all experiments in experimental ArrayList:
			for (int j = 0; j < speciesNames.size(); j++){
				Double m = modelValues.getModelEffluentValues().get(i).get(speciesNames.get(j));
				if(weightedRegression)
					SREG += Math.pow(m/(average.get(speciesNames.get(j))),2);
				else 
					SREG += Math.pow(m,2);	

			}
		
		//Ignition part:
		Double averageIgnition = null;
		if (weightedRegression) averageIgnition = experiments.getExperimentalValues().calcExperimentalIgnitionAverage();
		LinkedList<Double> modelIgnition = modelValues.getModelIgnitionValues();		
		for(int i=0;i<modelIgnition.size();i++){//Loop over all experiments in experimental ArrayList:
				Double m = modelValues.getModelIgnitionValues().get(i);
				if(weightedRegression)
					SREG += Math.pow(m/(averageIgnition),2);
				else 
					SREG += Math.pow(m,2);	
		}
		return SREG;
	}
	/**
	 * getSSQ returns the sum of residuals.
	 * @return
	 */
	public double getSRES(){
		Double sum = 0.0;
		if(experiments.getNoRegularExperiments()!=0){
			computeEffluentResid();	
			for (int i = 0; i < residEffluent.length; i++)		
				sum += residEffluent[i]*residEffluent[i];
		}
		if(experiments.getNoIgnitionDelayExperiments()!=0){
			computeIgnitionResid();
			for (int i = 0; i < residIgnition.length; i++)		
				sum += residIgnition[i]*residIgnition[i];
		}					
		return sum;
	}
	/**
	 * variance-covariance 'matrix' (in reality vector) based on error (y_i (experimental) - y^_i (model)) assuming that response variables are uncorrelated
	 * @return
	 */
	public Map<String,Double> getCovariance(){
		covarianceEffluent = new HashMap<String,Double>();
		LinkedList<String> responseVars = experiments.getResponseVariables();
		//average
		Double average = 0.0;
		for(Iterator<String> it = responseVars.iterator(); it.hasNext();){
			String s = (String) it.next();
			Double dummy = 0.0;
			for(int i = 0; i < experiments.getExperimentalValues().getExperimentalEffluentValues().size(); i++){
				dummy = dummy + (experiments.getExperimentalValues().getExperimentalEffluentValues().get(i).get(s) - modelValues.getModelEffluentValues().get(i).get(s));
			}
			dummy = dummy / experiments.getExperimentalValues().getExperimentalEffluentValues().size();
			average = dummy;

			Double dummy2 = 0.0;
			for (int i = 0; i < experiments.getExperimentalValues().getExperimentalEffluentValues().size(); i++){
				dummy2 = dummy2 + Math.pow((experiments.getExperimentalValues().getExperimentalEffluentValues().get(i).get(s) - modelValues.getModelEffluentValues().get(i).get(s)) - average,2);
			}
			dummy2 = dummy2 / experiments.getExperimentalValues().getExperimentalEffluentValues().size();

			covarianceEffluent.put(s,dummy2);
		}

		return covarianceEffluent;
	}

	/**
	 * computeResid computes every weigthed residual per experiment per response variable, i.e. (computed - observed) / sigma
	 * the value of sigma is debatable. it is now taken equal of the average value of each response variable
	 */
	public void computeEffluentResid(){
		Map<String,Double> average = null;
		if (weightedRegression) average = experiments.getExperimentalValues().calcExperimentalEffluentAverage();
		LinkedList<Map<String,Double>> experimentalEffluent = experiments.getExperimentalValues().getExperimentalEffluentValues();
		// we want to have a fixed order in which the keys are called, therefore we put the response var names in a String []
		LinkedList<String> speciesNames = experiments.getResponseVariables();

		int counter = 0;
		for(int i=0;i<experimentalEffluent.size();i++)//Loop over all experiments in experimental ArrayList:
			for (int j = 0; j < speciesNames.size(); j++){
				Double e = experimentalEffluent.get(i).get(speciesNames.get(j));
				Double m = modelValues.getModelEffluentValues().get(i).get(speciesNames.get(j));
				if(weightedRegression)
					residEffluent[counter] = (m-e)/(average.get(speciesNames.get(j)));
				else 
					residEffluent[counter] = (m-e);
				counter++;
			}					
	}
	public void computeIgnitionResid(){
		Double average = null;
		if (weightedRegression) average = experiments.getExperimentalValues().calcExperimentalIgnitionAverage();
		LinkedList<Double> experimentalIgnition = experiments.getExperimentalValues().getExperimentalIgnitionValues();
		// we want to have a fixed order in which the keys are called, therefore we put the response var names in a String []		
		for(int i=0;i<experimentalIgnition.size();i++){//Loop over all experiments in experimental ArrayList:
				Double e = experimentalIgnition.get(i);
				Double m = modelValues.getModelIgnitionValues().get(i);
				if(weightedRegression)
					residIgnition[i] = (m-e)/(average);
				else 
					residIgnition[i] = (m-e);
		}
	}
	/**
	 * @category getter
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Double [] getResidEffluent(){
		computeEffluentResid();
		return residEffluent;
	}
	/**
	 * @category getter
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Double [] getResidIgnition(){
		computeEffluentResid();
		return residIgnition;
	}

	public Experiments getExperiments() {
		return experiments;
	}

	public void setExperiments(Experiments experiments) {
		this.experiments = experiments;
	}
}
