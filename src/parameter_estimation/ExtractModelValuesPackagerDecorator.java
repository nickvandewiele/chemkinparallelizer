package parameter_estimation;

import datatypes.ModelValue;

public class ExtractModelValuesPackagerDecorator extends AbstractCKPackagerDecorator {

	AbstractCKPackager packager;

	
	public ExtractModelValuesPackagerDecorator(AbstractCKPackager packager){
		this.packager = packager;
		
		this.modelValues = new ModelValue[config.reactor_inputs.size()];
	}
	
	public AbstractCKEmulation []  runAllSimulations(){
		simulations = packager.runAllSimulations();
		
		for (int i = 0; i < simulations.length; i++){
			modelValues[i] = simulations[i].getModelValue(); 
		}
		
		return simulations;
	}
	
	public ModelValue[] getModelValues() {
		return modelValues;
	}

}
