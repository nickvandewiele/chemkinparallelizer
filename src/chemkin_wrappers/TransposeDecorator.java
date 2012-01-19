package chemkin_wrappers;

import parameter_estimation.ChemkinConstants;


public class TransposeDecorator extends ChemkinRoutineDecorator {

	AbstractChemkinRoutine routine;

	public TransposeDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		routine.keywords = new String [3];
		routine.keywords[0] = routine.config.paths.getBinDir()+"CKSolnTranspose";
		routine.keywords[1] = "-i";
		routine.keywords[2] = routine.config.paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT;

		return routine.keywords;

	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}

}
