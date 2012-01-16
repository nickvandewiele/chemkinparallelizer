package chemkin_wrappers;

import parameter_estimation.ChemkinConstants;

public class CreateSolnListDecorator extends AbstractChemkinRoutine {
	AbstractChemkinRoutine routine;

	public CreateSolnListDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}
	/**
	 * createSolnList creates the CKSolnList.txt file by calling the "GetSolution -listonly" routine<BR> 
	 */
	public String[] getKeyword() {
		String [] input = 
			{config.paths.getBinDir()+"GetSolution","-listonly",
				reactorDir+ChemkinConstants.XML};
		return input;
	}

	@Override
	public void executeCKRoutine() {
		routine.executeCKRoutine();
		
	}

}
