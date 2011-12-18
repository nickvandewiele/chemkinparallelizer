package parameter_estimation;

import org.apache.log4j.Logger;

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
			par_est.optimizeParameters();
			par_est.statistics();
			par_est.parity();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

	}

}
