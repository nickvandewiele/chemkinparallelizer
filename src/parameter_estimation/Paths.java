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
	protected String binDir;
	protected String chemInp;
	protected String [] reactorInputs;
	protected String outputDir;

	//no_licenses sets the limiting number for the counting semaphore
	protected int noLicenses;
	
	public Paths (String wd, String cd, String c_inp, String [] r_inp, int no_lic){
		workingDir = wd;
		chemkinDir = cd;
		chemInp = c_inp;
		reactorInputs = r_inp;
		outputDir = workingDir+"output/";
		binDir = chemkinDir+"/bin/";
		createOutputDir();
		noLicenses = no_lic;
		
	}
	protected void createOutputDir (){
		boolean temp = new File(outputDir).mkdir();
		if(!temp){
			logger.debug("Creation of output directory failed!");
			System.exit(-1);
		}
	}

	public String getOutputDir() {
		return outputDir;
	}
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	public String getWorkingDir() {
		return workingDir;
	}
	public String getChemkinDir() {
		return chemkinDir;
	}
	public String getChemInp() {
		return chemInp;
	}
	public String[] getReactorInputs() {
		return reactorInputs;
	}
	public int getNoLicenses() {
		return noLicenses;
	}
	public String getBinDir() {
		return binDir;
	}
}
