package parameter_estimation;

import datatypes.ModelValue;

public class ExtractModelValuesPackagerDecorator extends AbstractCKPackagerDecorator {

	AbstractCKPackager packager;

	
	public ExtractModelValuesPackagerDecorator(AbstractCKPackager packager){
		this.packager = packager;
		
		this.modelValues = new ModelValue[config.reactor_setup.length];
	}
	
	public AbstractCKEmulation []  runAllSimulations(){
		simulations = packager.runAllSimulations();
		
		for (int i = 0; i < simulations.length; i++){
			modelValues[i] = simulations[i].modelValue; 
		}
		
		return simulations;
	}
	
	public ModelValue[] getModelValues() {
		return modelValues;
	}

}
