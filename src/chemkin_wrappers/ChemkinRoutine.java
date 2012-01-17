package chemkin_wrappers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import parsers.ConfigurationInput;



/**
 * Implementation of {@link AbstractChemkinRoutine}
 * @author nmvdewie
 *
 */
public class ChemkinRoutine extends AbstractChemkinRoutine {
	
	static Logger logger = Logger.getLogger(ChemkinRoutine.class);
	
	public ChemkinRoutine(ConfigurationInput config,
			Runtime runtime) {
		
		this.config = config;
		
		this.runtime = runtime;
	}
	/**
	 *  this routine overloads the standard execute_CKRoutine with a specified working directory, 
	 *  different from the standard working directory
	 */
	@Override
	public void executeCKRoutine() {
		String s = null;
		String [] environment = null;
		Process p;
		try {
			if(reactorDir == null){
				p = runtime.exec(this.keywords);
			}
			else{
				p = runtime.exec(this.keywords, environment, new File(reactorDir));
			}
			
			BufferedReader stdInput_p = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError_p = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
			// read the output from the command
			logger.info("Here is the standard output of the command:\n");
			while ((s = stdInput_p.readLine()) != null) {
				logger.info(s);
			}
			stdInput_p.close();
			// read any errors from the attempted command
			logger.debug("Here is the standard error of the command (if any):\n");
			while ((s = stdError_p.readLine()) != null) {
				logger.debug(s);
			}
			stdError_p.close();
		

			p.waitFor();
	
			p.destroy();

		} catch (IOException e) {} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}
