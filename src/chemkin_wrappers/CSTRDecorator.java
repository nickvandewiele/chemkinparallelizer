package chemkin_wrappers;


public class CSTRDecorator extends ChemkinRoutineDecorator {

	AbstractChemkinRoutine routine;

	public CSTRDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		routine.keywords = new String [5];
		routine.keywords[0] = routine.config.paths.getBinDir()+"CKReactorGenericPSR";
		routine.keywords[1] = "-i";
		routine.keywords[2] = routine.reactorDir+routine.reactorSetup;
		routine.keywords[3] = "-o";
		routine.keywords[4] = reactorDir+reactorOut;
		
		return routine.keywords;
	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}
}
