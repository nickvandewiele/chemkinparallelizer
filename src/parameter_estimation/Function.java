package parameter_estimation;
import java.util.List;
import java.util.Map;


public class Function {
	public List<Map<String,Double>> model;
	public List<Map<String,Double>> exp;
	public double value;
	
	public Function (List<Map<String,Double>> m, List<Map<String,Double>> e){
		model = m;
		exp = e;
	}
	
	//defines the function to be minimized
	//SSQ : sum(model - exp)²
	//double func;
	//test function: rosenbrock banana's function, minimum for {x = 1, y = 1} of fun = 0
	//func = (1-x[0])*(1-x[0])+100*(x[1]-x[0]*x[0])*(x[1]-x[0]*x[0]);
	//func = 1+(params[0]*params[1])*(params[0]*params[1]);
	
	//sum: dummy var that will be used to calculate SSQ
	public double return_value(){
		Double sum = 0.0;
		double SSQ=0.0;
	
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
					sum = sum + (e-m)*(e-m);
				}
			}
			SSQ = sum.doubleValue();
		}
		return SSQ;
	}
		
}
