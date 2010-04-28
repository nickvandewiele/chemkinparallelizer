package parameter_estimation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Function {
	public List<Map<String,Double>> model;
	public List<Map<String,Double>> exp;
	public double[][]covariance;
	public double function_value;
	
	public Function (List<Map<String,Double>> m, List<Map<String,Double>> e){
		model = m;
		exp = e;
		covariance = new double[e.size()][e.get(0).size()];
	}
	
	//defines the function to be minimized
	//SSQ : sum(model - exp)²
	//double func;
	//test function: rosenbrock banana's function, minimum for {x = 1, y = 1} of fun = 0
	//func = (1-x[0])*(1-x[0])+100*(x[1]-x[0]*x[0])*(x[1]-x[0]*x[0]);
	//func = 1+(params[0]*params[1])*(params[0]*params[1]);
	
	//sum: dummy var that will be used to calculate SSQ
	public double return_SSQ(){
		Double sum = 0.0;
		double SSQ=0.0;
		Map<String,Double> cov = covariance();
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
					System.out.println("Variance: "+cov.get(s));
					sum = sum + (1 / cov.get(s) * (e-m) * (e-m));
				}
			}
			SSQ = sum.doubleValue();
		}
		return SSQ;
	}
	/**
	 * initialSSQ is identical to SSQ except for the variances, which are calculated using only experimental data
	 * @return
	 */
	public double return_initialSSQ(){
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
	public Map<String,Double> covariance(){
		Map<String,Double> covariance = new HashMap<String,Double>();
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
				dummy2 = dummy2 + (((exp.get(i).get(s) - model.get(i).get(s)) - average) * (exp.get(i).get(s) - model.get(i).get(s)));
			}
			dummy2 = dummy2 / exp.size();
			
			covariance.put(s,dummy2);
		}
		
		return covariance;
	}
		
}
