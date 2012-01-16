package chemkin_wrappers;


public class LaminarFlameDecorator extends AbstractChemkinRoutine {
	AbstractChemkinRoutine routine;

	public LaminarFlameDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		String [] input = 
			{
				routine.config.paths.getBinDir()+"CKReactorFreelyPropagatingFlame",
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
