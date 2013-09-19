package commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import parsers.ConfigurationInput;
import util.ChemkinConstants;
import chemkin_model.CKEmulation;
import chemkin_wrappers.AbstractChemkinRoutine;
import chemkin_wrappers.ChemkinRoutine;
import chemkin_wrappers.PreProcessDecorator;

public class CheckChemistryFileCommand implements Command {
	private static final String NO_ERRORS_FOUND_ON_INPUT = "NO ERRORS FOUND ON INPUT:";
	public static Logger logger = Logger.getLogger(CheckChemistryFileCommand.class);
	ConfigurationInput config;

	public CheckChemistryFileCommand(ConfigurationInput config){
		this.config = config;
	}
	public void execute() {
		CKEmulation c = new CKEmulation(config);
		//instantiation of parent chemkin routine:
		AbstractChemkinRoutine routine = new ChemkinRoutine(config);
		routine = new PreProcessDecorator(routine);//decoration of parent chemkin routine:
		routine.executeCKRoutine();//execution
		
		c.start();
		try {
			c.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		checkChemOutput();
		
		

	}
	/**
	 * checkChemInput does a preliminary check of the initial chemistry output file to verify if no errors are present.<BR>
	 * It calls the Chemkin preprocessor which produces the output file<BR>
	 * This output file is read, and the String  " NO ERRORS FOUND ON INPUT: " is sought.<BR>
	 * If this String is not present, System.exit(-1) is called<BR>
	 */
	public void checkChemOutput(){
		File file = new File(config.paths.getWorkingDir(),ChemkinConstants.CHEMOUT);
		boolean found = false;
		try {
			found = FileUtils.readFileToString(file).contains(NO_ERRORS_FOUND_ON_INPUT);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(!found){
			logger.debug("Initial chemistry input file contains errors. Revision required!");
			System.exit(-1);
		}

	}


}
