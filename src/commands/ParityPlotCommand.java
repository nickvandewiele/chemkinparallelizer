package commands;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import org.apache.log4j.Logger;

import chemkin_model.AbstractCKPackager;
import chemkin_model.CKPackager;
import chemkin_model.ExtractModelValuesPackagerDecorator;

import parameter_estimation.Chemistry;
import parameter_estimation.ChemkinConstants;
import parameter_estimation.Tools;
import parsers.ConfigurationInput;
import writers.ParityWriter;
import datamodel.ExperimentalValue;
import datamodel.ModelValue;

/**
 * Command implementation that performs the parity plot
 * option
 * @author nmvdewie
 *
 */
public class ParityPlotCommand implements Command {
	public static Logger logger = Logger.getLogger(ParityPlotCommand.class);
	ConfigurationInput config;

	public ParityPlotCommand(ConfigurationInput config){
		this.config = config;
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
		Command checkChemistry = new CheckChemistryFileCommand(config);
		checkChemistry.execute();

		AbstractCKPackager ckp = new CKPackager(config);
		ckp = new ExtractModelValuesPackagerDecorator(ckp);
		ckp.runAllSimulations();
		ModelValue[] modelValues = ckp.getModelValues();

		//read experimental data file:
		ExperimentalValue[] experimentalValues = config.experiments.getExperimentalData(); 

		String speciesPath = config.paths.getWorkingDir()+ChemkinConstants.CHEMASU;
		BufferedReader inSpecies = new BufferedReader (new FileReader(speciesPath));
		List<String> speciesNames = Chemistry.readSpeciesNames(inSpecies);

		ParityWriter writer = new ParityWriter(config.paths, experimentalValues, modelValues, speciesNames);
		writer.write();

		Tools.moveOutputFiles(config.paths);

		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for Parity Mode to finish: (sec) "+timeTook);
	}
}
