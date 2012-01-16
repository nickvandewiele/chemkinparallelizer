package chemkin_wrappers;


public class BatchDecorator extends AbstractChemkinRoutine {

	AbstractChemkinRoutine routine;

	public BatchDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		String [] input = 
			{
				routine.config.paths.getBinDir()+"CKReactorGenericClosed",
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
