package chemkin_wrappers;


public class ChemkinEnvDecorator extends ChemkinRoutineDecorator {


	public ChemkinEnvDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	public String[] getKeyword() {
		routine.keywords = new String [1];
		routine.keywords[0] = getConfig().paths.getBinDir()+"run_chemkin_env_setup.bat";

		return routine.keywords;
	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}

}
