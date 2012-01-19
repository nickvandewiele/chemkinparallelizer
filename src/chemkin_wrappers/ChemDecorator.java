package chemkin_wrappers;

import parameter_estimation.ChemkinConstants;


public class ChemDecorator extends ChemkinRoutineDecorator {

	AbstractChemkinRoutine routine;

	public ChemDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		
		routine.keywords = new String [7];
		routine.keywords[0] = routine.config.paths.getBinDir()+"chem";
		routine.keywords[1] = "-i";
		routine.keywords[2] = config.chemistry.getChemistryInput();
		routine.keywords[3] = "-o";
		routine.keywords[4] = ChemkinConstants.CHEMOUT;
		routine.keywords[5] = "-c";
		routine.keywords[6] = ChemkinConstants.CHEMASC;
		return routine.keywords;

	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}

}
