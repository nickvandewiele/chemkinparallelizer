package chemkin_wrappers;

import parameter_estimation.ChemkinConstants;


public class TransposeDecorator extends ChemkinRoutineDecorator {

	AbstractChemkinRoutine routine;

	public TransposeDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		String [] input = 
			{config.paths.getBinDir()+"CKSolnTranspose",
				reactorDir+ChemkinConstants.CKCSVNAME};
		return input;
	}

	@Override
	public void executeCKRoutine() {
		routine.executeCKRoutine();
		
	}

}
