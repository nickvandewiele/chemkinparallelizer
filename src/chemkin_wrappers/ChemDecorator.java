package chemkin_wrappers;

import parameter_estimation.ChemkinConstants;


public class ChemDecorator extends ChemkinRoutineDecorator {

	AbstractChemkinRoutine routine;

	public ChemDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		String [] input = 
			{config.paths.getBinDir()+"chem","-i",
				reactorDir+config.chemistry.getChemistryInput(),"-o",
				reactorDir+ChemkinConstants.CHEMOUT,"-c",
				reactorDir+ChemkinConstants.CHEMASC};
		return input;
	}

	@Override
	public void executeCKRoutine() {
		routine.executeCKRoutine();
		
	}

}
