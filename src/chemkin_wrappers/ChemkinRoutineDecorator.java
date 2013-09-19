package chemkin_wrappers;

import parsers.ConfigurationInput;


/**
 * Abstract decorator for chemkin routines, extending {@link AbstractChemkinRoutine}.
 * @author Nick
 *
 */
public abstract class ChemkinRoutineDecorator extends AbstractChemkinRoutine {

	AbstractChemkinRoutine routine;
	
	/**
	 * the required getter that returns the specific keywords calling a chemkin routine. 
	 * @return
	 */
	public abstract String[] getKeyword();
	
	
	@Override
	public String getReactorDir() {
		return routine.getReactorDir();
	}

	@Override
	public String getReactorSetup() {
		return routine.getReactorSetup();
	}

	@Override
	public String getReactorOut() {
		return routine.getReactorOut();
	}
	
	@Override
	public ConfigurationInput getConfig() {
		return routine.getConfig();
	}
}
