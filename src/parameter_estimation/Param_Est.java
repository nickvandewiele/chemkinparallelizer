package parameter_estimation;
import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

import readers.ConfigurationInput;
import readers.ReactorInput;
import readers.ReactorInputParsable;
import readers.PFRReactorInputParser1;


public class Param_Est extends Loggable{

	ConfigurationInput config;

	private Fitting fitting;

	private LinkedList<String> speciesNames;

	/**
	 * @category getter
	 * @return
	 */

	private ModelValue[] modelValues;
	/**
	 * @category setter
	 * @param modelValues
	 */
	public void setModelValues(ModelValues modelValues) {
		this.modelValues = modelValues;
	}

	public Param_Est(ConfigurationInput config) {
		this.config = config;

		/**
		 * create reactor input files, if necessary:
		 */
		if(experiments.isFlagReactorDB()){
			experiments.getReactorInputCollector().setRegularInputs(createRegularReactorInputs());
		}

		//TODO should check whether merge is successful
		//fill ReactorInputs with RegularReactorInputs, IgnitionDelayInputs:
		experiments.getReactorInputCollector().mergeReactorInputs();

		//set flags for ignition delays:
		experiments.getReactorInputCollector().setFlagIgnitionDelays();

		//set flags for flame speeds:
		experiments.getReactorInputCollector().setFlagFlameSpeeds();
	}
	private void checkChemistryFile(Runtime r) throws IOException,
	InterruptedException, FileNotFoundException {
		CKEmulation c = new CKEmulation(paths, chemistry, r);
		c.preProcess(r);
		BufferedReader in = new BufferedReader(new FileReader(paths.getWorkingDir()+ChemkinConstants.CHEMOUT));
		c.checkChemOutput(in);
		c.join();
	}
	/**
	 * @category 
	 * @return
	 * @throws IOException
	 */
	public ModelValues getModelValues(CKPackager ckp) {
		//check whether model values have already been stored:
		try {
			modelValues = ckp.getModelValues();
		} catch (Exception e) {
			logger.debug(e);
		}
		return modelValues;

	}
	protected void moveOutputFiles (){
		Tools.moveFiles(paths.getWorkingDir(), paths.getOutputDir(), ".out");
		Tools.moveFiles(paths.getWorkingDir(), paths.getOutputDir(), ".asu");
		Tools.moveFiles(paths.getWorkingDir(), paths.getOutputDir(), ".input");
		Tools.moveFiles(paths.getWorkingDir(), paths.getOutputDir(), ".asc");
		Tools.moveFile(paths.getOutputDir(),"SpeciesParity.csv");
		Tools.moveFile(paths.getOutputDir(),"IgnitionDelayParity.csv");
		Tools.moveFile(paths.getOutputDir(),"FlameSpeedParity.csv");
		Tools.moveFile(paths.getOutputDir(),"CKSolnList.txt");

	}
	public LinkedList<String> createRegularReactorInputs(){
		LinkedList<String> RegularReactorInputs = new LinkedList<String>();
		ReactorInputParsable parser;
		if (experiments.isFlagReactorDB()){
				if(experiments.getFlagReactorSetupType()==1){
					try {		
						parser = new PFRReactorInputParser1(config.getWorking_dir()+experiments.getReactorSetupDB());		
						RegularReactorInputs = parser.parse();
					} catch (IOException e) {
						logger.debug(e);
					}
				}
		}
		return RegularReactorInputs;
	}
	public void writeParameters(PrintWriter out){
		logger.info("New values of parameters are: ");
		StringBuffer stringBuff = new StringBuffer();
		for (int i = 0; i < chemistry.getParams().getBeta().length; i++) {
			stringBuff.append("Reaction "+i+": \n");
			for (int j = 0; j < chemistry.getParams().getBeta()[0].length; j++){
				stringBuff.append(chemistry.getParams().getBeta()[i][j]+", \n");
			}
			stringBuff.append("\n");
		}
		out.print(stringBuff.toString());
		out.close();


	}
}
