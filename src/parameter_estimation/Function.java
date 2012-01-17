package parameter_estimation;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


import datatypes.EffluentResidual;
import datatypes.ExperimentalValue;
import datatypes.ModelValue;
import datatypes.Residual;
import datatypes.Residual.TYPE;
import datatypes.ResidualFactory;


public class Function extends Loggable{
	
	private ExperimentalValue[] experimentalValues;
	private ModelValue[] modelValues;

	Residual[] residuals;
	
	private Map<String,Double> covarianceEffluent = new HashMap<String,Double>();
	
	public double functionValue;
	
	//boolean weighted_regression determines whether a weighted or an unweighted regression will be applied.
	private boolean weightedRegression = true;
	

	//TODO most of the methods here should be in Statistics...
	public Function (ExperimentalValue[] experiments, ModelValue[] modelValues){
		this.experimentalValues = experiments;
		this.modelValues = modelValues;
		
		computeResiduals();
		
	}

	private void computeResiduals() {
		residuals = new Residual[experimentalValues.length];
		ResidualFactory factory = new ResidualFactory();
		for(int i = 0; i < experimentalValues.length; i++){
			Residual residual = factory.createResidual(experimentalValues[i], modelValues[i]);
			residuals[i] = residual;
			
		}
		
	}

	/**
	 * returns the sum of the regression (model) values
	 * 
	 * @return
	 */
	public double getSREG(){
		double SREG = 0.0;
		
		for(ModelValue modelValue: modelValues){
			SREG += modelValue.getSSQValue();
		}

		return SREG;
	}
	/**
	 * getSSQ returns the sum of residuals.
	 * @return
	 */
	public double getSRES(){
		Double sum = 0.0;
		
		for(Residual residual : residuals){
			sum+=residual.getSSQValue();
		}

		return sum;
	}
	/**
	 * variance-covariance 'matrix' (in reality vector) based on error (y_i (experimental) - y^_i (model)) assuming that response variables are uncorrelated
	 * @return
	 */
	public Map<String,Double> getCovariance(){
		covarianceEffluent = new HashMap<String,Double>();
		LinkedList<String> responseVars = experimentalValues.getResponseVariables().getEffluentResponses();
		//average
		Double average = 0.0;
		
		for(Iterator<String> it = responseVars.iterator(); it.hasNext();){
			String s = (String) it.next();
			Double dummy = 0.0;
			for(int i = 0; i < experimentalValues.getExperimentalValues().getExperimentalEffluentValues().size(); i++){
				dummy = dummy + (experimentalValues.getExperimentalValues().getExperimentalEffluentValues().get(i).get(s) - modelValues.getModelEffluentValues().get(i).get(s));
			}
			dummy = dummy / experimentalValues.getExperimentalValues().getExperimentalEffluentValues().size();
			average = dummy;

			Double dummy2 = 0.0;
			for (int i = 0; i < experimentalValues.getExperimentalValues().getExperimentalEffluentValues().size(); i++){
				dummy2 = dummy2 + Math.pow((experimentalValues.getExperimentalValues().getExperimentalEffluentValues().get(i).get(s) - modelValues.getModelEffluentValues().get(i).get(s)) - average,2);
			}
			dummy2 = dummy2 / experimentalValues.getExperimentalValues().getExperimentalEffluentValues().size();

			covarianceEffluent.put(s,dummy2);
		}

		return covarianceEffluent;
	}

	public double[] getResid() {
		
    	double [] resid = new double[residuals.length];
    	int i = 0;
    	while(i < resid.length){
    		if(residuals[i].type.equals(TYPE.PRODUCT_EFFLUENT)){
    			Iterator<Double> iter = ((EffluentResidual)residuals[i]).createIterator();
    			while(iter.hasNext()){
    				resid[i] = iter.next();
    			}
    		}
    		else{
    			resid[i] = residuals[i].getValue();
        		i++;	
    		}
    	}
    	return resid;
	}

}
