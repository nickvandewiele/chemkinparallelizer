package chemkin_wrappers;


public class BatchDecorator extends ChemkinRoutineDecorator {

	AbstractChemkinRoutine routine;

	public BatchDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		routine.keywords = new String [5];
		routine.keywords[0] = routine.config.paths.getBinDir()+"CKReactorGenericClosed";
		routine.keywords[1] = "-i";
		routine.keywords[2] = routine.getReactorDir()+routine.reactorSetup;
		routine.keywords[3] = "-o";
		routine.keywords[4] = routine.getReactorDir()+reactorOut;
	
		return routine.keywords;
	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}


}
