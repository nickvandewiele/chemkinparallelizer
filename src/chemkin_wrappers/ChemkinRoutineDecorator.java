package chemkin_wrappers;

import parsers.ConfigurationInput;


public abstract class ChemkinRoutineDecorator extends AbstractChemkinRoutine {

	AbstractChemkinRoutine routine;
	
	public abstract String[] getKeyword();
	
	
	public String getReactorDir() {
		return routine.getReactorDir();
	}

	public String getReactorSetup() {
		return routine.getReactorSetup();
	}

	public String getReactorOut() {
		return routine.getReactorOut();
	}
	
	public ConfigurationInput getConfig() {
		return routine.getConfig();
	}
}
