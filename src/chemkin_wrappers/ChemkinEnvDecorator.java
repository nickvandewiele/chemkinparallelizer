package chemkin_wrappers;

import util.Paths;


/**
 * Decorator for {@link AbstractChemkinRoutine} that calls the batch "run_chemkin_env_setup.bat" of Chemkin.
 * 
 * The batch command sets a couple of environment variables required for chemkin to be run through the GUI. 
 * @author Nick
 *
 */
public class ChemkinEnvDecorator extends ChemkinRoutineDecorator {


	public ChemkinEnvDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	@Override
	public String[] getKeyword() {
		routine.keywords = new String [1];
		routine.keywords[0] = Paths.getBinDirLocation()+"run_chemkin_env_setup.bat";

		return routine.keywords;
	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}

}
