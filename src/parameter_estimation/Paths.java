package parameter_estimation;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * Paths contains paths to directories, files that are important in the Parameter Estimation program<BR>
 * Paths serves as a supertype to Param_Est, CKPackager, Rosenbrock types
 * @author nmvdewie
 *
 */
public class Paths {
	static Logger logger = Logger.getLogger(ParameterEstimationDriver.logger.getName());
	protected String workingDir;
	protected String chemkinDir;
	/**
	 * @category setter
	 * @return
	 */
	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
		setOutputDir();
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setChemkinDir(String chemkinDir) {
		this.chemkinDir = chemkinDir;
		setBinDir();
	}
	/**
	 * @category setter
	 * @return
	 */
	private void setBinDir() {
		binDir = chemkinDir+"/bin/";
	}
	protected String binDir;
	protected String outputDir;

	public Paths(String wd, String cd){
		workingDir = wd;
		chemkinDir = cd;
		binDir = chemkinDir+"/bin/";
		checkOutputDir();		
	}
	public Paths(){
		
	}
	protected void checkOutputDir (){
		boolean temp = new File(outputDir).mkdir();
		if(!temp){
			logger.debug("Creation of output directory failed!");
			System.exit(-1);
		}
	}
	/**
	 * @category getter
	 * @return
	 */
	public String getOutputDir() {
		return outputDir;
	}
	/**
	 * @category setter
	 * @return
	 */
	private void setOutputDir() {
		outputDir = workingDir+"output/";
		checkOutputDir();
	}
	/**
	 * @category getter
	 * @return
	 */
	public String getWorkingDir() {
		return workingDir;
	}
	/**
	 * @category getter
	 * @return
	 */
	public String getChemkinDir() {
		return chemkinDir;
	}
	/**
	 * @category getter
	 * @return
	 */
	public String getBinDir() {
		return binDir;
	}
}
