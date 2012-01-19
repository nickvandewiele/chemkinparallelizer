package chemkin_wrappers;

import parameter_estimation.ChemkinConstants;


public class TransposeDecorator extends ChemkinRoutineDecorator {

	public TransposeDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	public String[] getKeyword() {
		routine.keywords = new String [3];
		routine.keywords[0] = getConfig().paths.getBinDir()+"CKSolnTranspose";
		routine.keywords[1] = "-i";
		routine.keywords[2] = getConfig().paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT;

		return routine.keywords;

	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}

}
