package parameter_estimation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.runners.ParentRunner;

import datatypes.ExperimentalValue;

/**
 * Command that executes the statistics calculation via
 * the Param_Est object
 * @author nmvdewie
 *
 */
public class StatisticsCommand implements Command {
	public static Logger logger = Logger.getLogger(StatisticsCommand.class);
	Param_Est par_est;

	public StatisticsCommand(Param_Est p){
		this.par_est = p;
	}
	public void execute() {
		logger.info("STATISTICS MODE");
		try {
			statistics();
			Command parity = new ParityPlotCommand(par_est);
			parity.execute();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public void statistics() throws Exception{
		long time = System.currentTimeMillis();

		//check if initial input file is error-free:
		Command checkChemistry = new CheckChemistryFileCommand(par_est.config);
		checkChemistry.execute();

		// take initial guesses from chem.inp file:
		par_est.config.chemistry.getParams().setBeta(par_est.config.chemistry.initialGuess(par_est.config.paths.getWorkingDir()));

		//read experimental data file:
		ExperimentalValue[] experimentalValues = par_est.config.experiments.getExperimentalData(); 

		Optimization optimization = new Optimization(par_est.config);

		optimization.calcStatistics();
		//moveOutputFiles();
		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this optimization to finish: (sec) "+timeTook);	    	    
	}

	
}
