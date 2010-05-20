package parameter_estimation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Function {
	public List<Map<String,Double>> model;
	private List<Map<String,Double>> exp;

	// resid is a double[no_experiments] that contains the residual value (y - f(x,beta)) for each experiment separately.
	private double [][] resid;
	private Map<String,Double> covariance = new HashMap<String,Double>();
	public double function_value;
	
	public Function (List<Map<String,Double>> m, List<Map<String,Double>> e){
		model = m;
		exp = e;
		resid = new double[exp.size()][e.get(0).size()];
	}
	
	/**
	 * getSSQ returns the sum of residuals.
	 * @return
	 */
	public double getSSQ(){
		computeResid();
		Double sum = 0.0;
		for (int i = 0; i < resid.length; i++){
			for (int j = 0; j < resid[0].length; j++){
				sum += resid[i][j]*resid[i][j];
			}
		}
		
		return sum;
	}
	/**
	 * initialSSQ is identical to SSQ except for the variances, which are calculated using only experimental data
	 * @return
	 */
	public double getInitialSSQ(){
		Double sum = 0.0;
		double SSQ=0.0;
		Map<String,Double> cov = initial_covariance();
		//check if size of experiment list is equal to size of model list:
		if (exp.size() != model.size()) {
			System.out.println("Experiment ArrayList has different number of experiments as the model ArrayList!");
		}
		else {
			//Loop over all experiments in experimental ArrayList:
			for(int i=0;i<exp.size();i++)
			{
				//Loop over all keys in experiment i:
				for ( String s : exp.get(i).keySet()){
					Double e = exp.get(i).get(s);
					Double m = model.get(i).get(s);
					System.out.println(s);
					System.out.println(cov.get(s));
					sum = sum + (1 / cov.get(s) * (e-m) * (e-m));
				}
			}
			SSQ = sum.doubleValue();
		}
		return SSQ;
	}
	/**
	 * variance-covariance 'matrix' (in reality vector) based on only response variable data (experimental) assuming that response variables are uncorrelated
	 * @return
	 */
	public Map<String,Double> initial_covariance(){
		Map<String,Double> init_covariance = new HashMap<String,Double>();
		Set<String> response_vars = exp.get(0).keySet();
		Double average = 0.0;
		for(Iterator<String> it = response_vars.iterator(); it.hasNext();){
			String s = (String) it.next();
			Double dummy = 0.0;
			for(int i = 0; i < exp.size(); i++){
				dummy = dummy + exp.get(i).get(s);
			}
			dummy = dummy / exp.size();
			average = dummy;
			
			Double dummy2 = 0.0;
			for (int i = 0; i < exp.size(); i++){
				dummy2 = dummy2 + (exp.get(i).get(s) - average) * (exp.get(i).get(s) - average);
			}
			dummy2 = dummy2 / exp.size();
			
			init_covariance.put(s,dummy2);
		}
		
		return init_covariance;
	}
	
	/**
	 * variance-covariance 'matrix' (in reality vector) based on error (y_i (experimental) - y^_i (model)) assuming that response variables are uncorrelated
	 * @return
	 */
	public Map<String,Double> getCovariance(){
		covariance = new HashMap<String,Double>();
		Set<String> response_vars = exp.get(0).keySet();
		Double average = 0.0;
		for(Iterator<String> it = response_vars.iterator(); it.hasNext();){
			String s = (String) it.next();
			Double dummy = 0.0;
			for(int i = 0; i < exp.size(); i++){
				dummy = dummy + (exp.get(i).get(s) - model.get(i).get(s));
			}
			dummy = dummy / exp.size();
			average = dummy;

			Double dummy2 = 0.0;
			for (int i = 0; i < exp.size(); i++){
				dummy2 = dummy2 + Math.pow((exp.get(i).get(s) - model.get(i).get(s)) - average,2);
			}
			dummy2 = dummy2 / exp.size();
			
			covariance.put(s,dummy2);
		}
		
		return covariance;
	}
	/**
	 * computeResid computes every weigthed residual per experiment per response variable, i.e. (computed - observed) / sigma
	 */
	public void computeResid(){
		Map<String,Double> cov = getCovariance();
		
		// we want to have a fixed order in which the keys are called, therefore we put the response var names in a String []
		String [] species_names = new String [exp.get(0).size()];
		int counter = 0;
		for (String s : exp.get(0).keySet()){
			species_names[counter] = s;
			counter++;
		}
		
		//initiation of resid:
		resid = new double[exp.size()][exp.get(0).size()];
		

		if (exp.size() != model.size()) {//check if size of experiment list is equal to size of model list:
			System.out.println("Experiment ArrayList has different number of experiments as the model ArrayList!");
		}
		else {
			for(int i=0;i<exp.size();i++)//Loop over all experiments in experimental ArrayList:
			{
				for (int j = 0; j < species_names.length; j++){
					Double e = exp.get(i).get(species_names[j]);
					Double m = model.get(i).get(species_names[j]);
					resid[i][j] = (1 / Math.sqrt(cov.get(species_names[j]))) * (m-e) ;
				}
/*				for ( String s : exp.get(i).keySet()){//Loop over all keys in experiment i:
					Double e = exp.get(i).get(s);
					Double m = model.get(i).get(s);
					System.out.println(s);
				
					sum = sum + (1 / cov.get(s)) * (m-e) ;
				}
*/				

			}
		}
	}
	public double [][] getResid(){
		computeResid();
		return resid;
	}
}
