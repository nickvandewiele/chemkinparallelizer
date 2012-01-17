package parameter_estimation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import datatypes.ExperimentalValue;
import datatypes.ModelValue;

/**
 * Command implementation that performs the parity plot
 * option
 * @author nmvdewie
 *
 */
public class ParityPlotCommand implements Command {
	public static Logger logger = Logger.getLogger(ParityPlotCommand.class);
	Param_Est parameter_estimation;

	public ParityPlotCommand(Param_Est p){
		this.parameter_estimation = p;
	}
	public void execute() {
		logger.info("PARITY PLOT MODE");
		try {
			parity();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//TODO parity should be type, not method, i believe
	public void parity() throws Exception{
		long time = System.currentTimeMillis();

		//check if initial input file is error-free:
		Command checkChemistry = new CheckChemistryFileCommand(parameter_estimation.config);
		checkChemistry.execute();

		AbstractCKPackager ckp = new CKPackager(parameter_estimation.config);
		ckp = new ExtractModelValuesPackagerDecorator(ckp);
		ckp.runAllSimulations();
		ModelValue[] modelValues = ckp.modelValues;

		//read experimental data file:
		ExperimentalValue[] experimentalValues = parameter_estimation.config.experiments.getExperimentalData(); 

		String speciesPath = parameter_estimation.config.paths.getWorkingDir()+ChemkinConstants.CHEMASU;
		BufferedReader inSpecies = new BufferedReader (new FileReader(speciesPath));
		List<String> speciesNames = Chemistry.readSpeciesNames(inSpecies);

		ParityWriter writer = new ParityWriter(parameter_estimation.config.paths, experimentalValues, modelValues, speciesNames);
		writer.write();

		Tools.moveOutputFiles(parameter_estimation.config.paths);

		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for Parity Mode to finish: (sec) "+timeTook);
	}
}
