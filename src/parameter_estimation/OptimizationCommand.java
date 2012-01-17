package parameter_estimation;

import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import datatypes.ExperimentalValue;

/**
 * Command implementation that performs the kinetic parameter optimization
 * @author nmvdewie
 *
 */
public class OptimizationCommand implements Command {
	public static Logger logger = Logger.getLogger(OptimizationCommand.class);
	Param_Est par_est;

	public OptimizationCommand(Param_Est p){
		this.par_est = p;
	}

	public void execute() {
		logger.info("PARAMETER OPTIMIZATION MODE");
		try {
			optimizeParameters();
			Command statisticsCommand = new StatisticsCommand(par_est);
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
		Command checkChemistry = new CheckChemistryFileCommand(par_est.config);
		checkChemistry.execute();
		
		// take initial guesses from chem.inp file:
		par_est.config.chemistry.initialGuess(par_est.config.paths.getWorkingDir());
		logger.info("Initial Guesses of parameters are:");
		//Printer.printMatrix(par_est.config.chemistry.getParams().getBeta(),System.out);

		//read experimental data file:
		ExperimentalValue[] experimentalValues = par_est.config.experiments.getExperimentalData(); 

		Optimization optimization = new Optimization(par_est.config);

		//call optimization routine:
		double [][]beta = optimization.optimize();
		par_est.config.chemistry.getParams().setBeta(beta);

		//write optimized parameters:
		PrintWriter out = new PrintWriter(new FileWriter("params.txt"));
		writeParameters(out);
		Tools.moveFile(par_est.config.paths.getOutputDir(), "params.txt");

		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this optimization to finish: (sec) "+timeTook);

	}
	

	public void writeParameters(PrintWriter out){
		logger.info("New values of parameters are: ");
		StringBuffer stringBuff = new StringBuffer();
		for (int i = 0; i < par_est.config.chemistry.getParams().getBeta().length; i++) {
			stringBuff.append("Reaction "+i+": \n");
			for (int j = 0; j < par_est.config.chemistry.getParams().getBeta()[0].length; j++){
				stringBuff.append(par_est.config.chemistry.getParams().getBeta()[i][j]+", \n");
			}
			stringBuff.append("\n");
		}
		out.print(stringBuff.toString());
		out.close();


	}
}
