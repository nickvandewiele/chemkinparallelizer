package chemkin_wrappers;


public class PremixedFlameDecorator extends AbstractChemkinRoutine {

	AbstractChemkinRoutine routine;

	public PremixedFlameDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		String [] input = 
			{
				routine.config.paths.getBinDir()+"CKReactorBurnerStabilizedFlame",
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
