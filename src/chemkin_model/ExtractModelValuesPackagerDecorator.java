package chemkin_model;

import datamodel.ModelValue;

/**
 * The type extracts the model values from simulations array into an attribute in the 
 * packager type itself.
 * @author Nick
 *
 */
public class ExtractModelValuesPackagerDecorator extends AbstractCKPackagerDecorator {
	
	public ExtractModelValuesPackagerDecorator(AbstractCKPackager packager){
		super.packager = packager;
		
		super.packager.modelValues = new ModelValue[getConfig().reactor_inputs.size()];
	}
	
	public AbstractCKEmulation []  runAllSimulations(){
		simulations = packager.runAllSimulations();
		
		for (int i = 0; i < simulations.length; i++){
			packager.getModelValues()[i] = simulations[i].getModelValue(); 
		}
		
		return simulations;
	}
	
	public ModelValue[] getModelValues() {
		return packager.getModelValues();
	}

}
