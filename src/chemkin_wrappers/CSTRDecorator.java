package chemkin_wrappers;


public class CSTRDecorator extends ChemkinRoutineDecorator {

	public CSTRDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	public String[] getKeyword() {
		routine.keywords = new String [5];
		routine.keywords[0] = getConfig().paths.getBinDir()+"CKReactorGenericPSR";
		routine.keywords[1] = "-i";
		routine.keywords[2] = getReactorDir()+getReactorSetup();
		routine.keywords[3] = "-o";
		routine.keywords[4] = getReactorDir()+getReactorOut();
		
		return routine.keywords;
	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}
}
