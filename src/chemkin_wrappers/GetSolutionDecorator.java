package chemkin_wrappers;

import parameter_estimation.ChemkinConstants;

public class GetSolutionDecorator extends AbstractChemkinRoutine {

	AbstractChemkinRoutine routine;

	public GetSolutionDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		//String abbrev_path = cd+"data/abbreviations.csv";
		/**
		 * nosen: no sensitivity data is included
		 * norop: no rate of production info is included
		 */
		String [] progGetSol = {config.paths.getBinDir()+"GetSolution",
				"-nosen","-norop","-mass",
				reactorDir+ChemkinConstants.XML};
		return progGetSol;
	}

	@Override
	public void executeCKRoutine() {
		routine.executeCKRoutine();

	}

}
