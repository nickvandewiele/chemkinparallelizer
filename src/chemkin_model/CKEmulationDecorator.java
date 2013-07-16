package chemkin_model;

import parsers.ConfigurationInput;
import readers.ReactorInput;
import datamodel.ModelValue;

/**
 * Supertype for decorators for {@link AbstractCKEmulation} that will add new behaviour
 * for particular chemkin simulations such as 
 * <LI>the first one (that needs to create a CKSolnList)
 * @author nmvdewie
 *
 */
public abstract class CKEmulationDecorator extends AbstractCKEmulation {

	public AbstractCKEmulation simulation;
	
	@Override
	public abstract void run();

	public ConfigurationInput getConfig(){
		return simulation.getConfig();
	}
	
	public String getReactorDir() {
		return simulation.getReactorDir();
	}

	public ReactorInput getReactorInput() {
		return simulation.getReactorInput();
	}

	public String getReactorOut() {
		return simulation.getReactorOut();
	}
	
	public ModelValue getModelValue() {
		return simulation.getModelValue();
	}
	
}
