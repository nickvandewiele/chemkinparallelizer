package chemkin_wrappers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;




/**
 * Implementation of {@link AbstractChemkinRoutine} that provides an
 * implementation for the executeCKRoutine, namely executing a process with the 
 * keywords specified in the keywords string array.
 * @author nmvdewie
 *
 */
public class ChemkinRoutine extends AbstractChemkinRoutine {
	
	static Logger logger = Logger.getLogger(ChemkinRoutine.class);
	
	public ChemkinRoutine() {

	}
	/**
	 *  This routine overloads the abstract executeCKRoutine with a specified working directory, 
	 *  different from the standard working directory
	 */
	@Override
	public void executeCKRoutine() {
		String s = null;
		Process p;
		try {
			if(getReactorDir() == null){
				p = Runtime.getRuntime().exec(this.keywords);
			}
			else{
				p = Runtime.getRuntime().exec(this.keywords, null, new File(getReactorDir()));//environment = null
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
