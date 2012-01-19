package parameter_estimation;

import datatypes.ModelValue;

public class ExtractModelValuesPackagerDecorator extends AbstractCKPackagerDecorator {
	
	public ExtractModelValuesPackagerDecorator(AbstractCKPackager packager){
		super.packager = packager;
		
		super.packager.modelValues = new ModelValue[getConfig().reactor_inputs.size()];
	}
	
	public AbstractCKEmulation []  runAllSimulations(){
		simulations = packager.runAllSimulations();
		
		for (int i = 0; i < simulations.length; i++){
			packager.modelValues[i] = simulations[i].getModelValue(); 
		}
		
		return simulations;
	}
	
	public ModelValue[] getModelValues() {
		return modelValues;
	}

}
