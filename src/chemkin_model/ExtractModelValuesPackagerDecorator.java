package chemkin_model;

import parsers.ConfigurationInput;
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
		
		super.packager.modelValues = new ModelValue[ConfigurationInput.reactor_inputs.size()];
	}
	
	@Override
	public AbstractCKEmulation []  runAllSimulations(){
		simulations = packager.runAllSimulations();
		
		for (int i = 0; i < simulations.length; i++){
			packager.getModelValues()[i] = simulations[i].getModelValue(); 
		}
		
		return simulations;
	}
	
	@Override
	public ModelValue[] getModelValues() {
		return packager.getModelValues();
	}

}
