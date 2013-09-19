package commands;

import java.io.FileWriter;
import java.io.PrintWriter;

import optimization.Optimization;

import org.apache.log4j.Logger;

import parsers.ConfigurationInput;
import util.Paths;
import util.Tools;
import datamodel.ExperimentalValue;

/**
 * Command implementation that performs the kinetic parameter optimization
 * @author nmvdewie
 *
 */
public class OptimizationCommand implements Command {
	public static Logger logger = Logger.getLogger(OptimizationCommand.class);
	ConfigurationInput config;

	public OptimizationCommand(ConfigurationInput config){
		this.config = config;
	}

	public void execute() {
		logger.info("PARAMETER OPTIMIZATION MODE");
		try {
			optimizeParameters();
			Command statisticsCommand = new StatisticsCommand(config);
			statisticsCommand.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	/**
	 * optimizeParameters is the method that will optimize the kinetic parameters. It does so by:<BR>
	 * <LI>checking the validity of the par_est.config.chemistry input file</LI>
	 * <LI>taking the initial guesses of the kinetic parameters from the par_est.config.chemistry input file</LI>
	 * <LI>reading the experimental database and store these values for the response variables</LI>
	 * <LI>calling the actual optimization routine, i.e. the Rosenbrock algorithm</LI>
	 * <LI>writing the optimized kinetic parameters to a params.txt file</LI>	
	 * @throws Exception 
	 * @throws Exception 
	 */
	public void optimizeParameters() throws Exception{
		long time = System.currentTimeMillis();

		//check if initial input file is error-free:
		Command checkChemistry = new CheckChemistryFileCommand(config);
		checkChemistry.execute();
		
		// take initial guesses from chem.inp file:
		config.chemistry.initialGuess();
		logger.info("Initial Guesses of parameters are:");
		//Printer.printMatrix(par_est.config.chemistry.getParams().getBeta(),System.out);

		//read experimental data file:
		ExperimentalValue[] experimentalValues = config.experiments.getExperimentalData(); 

		Optimization optimization = new Optimization(config);

		//call optimization routine:
		double [][]beta = optimization.optimize();
		config.chemistry.getParams().setBeta(beta);

		//write optimized parameters:
		PrintWriter out = new PrintWriter(new FileWriter("params.txt"));
		writeParameters(out);
		Tools.moveFile(Paths.getOutputDir(), "params.txt");

		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this optimization to finish: (sec) "+timeTook);

	}
	

	public void writeParameters(PrintWriter out){
		logger.info("New values of parameters are: ");
		StringBuffer stringBuff = new StringBuffer();
		for (int i = 0; i < config.chemistry.getParams().getBeta().length; i++) {
			stringBuff.append("Reaction "+i+": \n");
			for (int j = 0; j < config.chemistry.getParams().getBeta()[0].length; j++){
				stringBuff.append(config.chemistry.getParams().getBeta()[i][j]+", \n");
			}
			stringBuff.append("\n");
		}
		out.print(stringBuff.toString());
		out.close();


	}
}
