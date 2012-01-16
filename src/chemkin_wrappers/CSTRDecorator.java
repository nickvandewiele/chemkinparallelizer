package chemkin_wrappers;


public class CSTRDecorator extends AbstractChemkinRoutine {

	AbstractChemkinRoutine routine;

	public CSTRDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		String [] input = 
			{
				routine.config.paths.getBinDir()+"CKReactorGenericPSR",
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
