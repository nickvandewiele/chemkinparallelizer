package chemkin_wrappers;


public class PFRDecorator extends ChemkinRoutineDecorator {

	AbstractChemkinRoutine routine;

	public PFRDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		String [] input = 
			{
				routine.config.paths.getBinDir()+"CKReactorPlugFlow",
				"-i",routine.reactorDir+routine.reactorSetup,
				"-o",reactorDir+reactorOut
			};
		return input;
	}

	@Override
	public void executeCKRoutine() {
		routine.executeCKRoutine();
		
	}

}
