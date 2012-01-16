package chemkin_wrappers;


public class ChemkinEnvDecorator extends ChemkinRoutineDecorator {

	AbstractChemkinRoutine routine;

	public ChemkinEnvDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		String [] input = 
			{config.paths.getBinDir()+"run_chemkin_env_setup.bat"};
		return input;
	}

	@Override
	public void executeCKRoutine() {
		routine.executeCKRoutine();
		
	}

}
