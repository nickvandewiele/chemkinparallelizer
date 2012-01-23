package parameter_estimation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import chemkin_wrappers.AbstractChemkinRoutine;
import chemkin_wrappers.ChemkinRoutine;
import chemkin_wrappers.GetSolutionDecorator;
import parsers.ConfigurationInput;
import readers.ReactorInput;
import readers.ReactorSetupInput;

/**
 * CKPackager is a type that bundles all executed CKEmulations into one data structure. 
 * It corresponds to the set of experiments that are executed and are structured in the same way
 * the reactor input files are provided in the input.xml file. 
 * @author nmvdewie
 *
 */
public class CKPackager extends AbstractCKPackager{

	//constructor for parameter optimization option:
	public CKPackager(ConfigurationInput config){
		this.config = config;
		int length = config.reactor_inputs.size();
		simulations = new AbstractCKEmulation[length];
	}

	public AbstractCKEmulation []  runAllSimulations(){
		Runtime rt = Runtime.getRuntime();
		Semaphore semaphore = new Semaphore(getConfig().licenses.getValue()); 
		/*
		 * First simulation:
		 * wait to start other threads before the first thread, 
		 * creating the CKSolnList.txt is completely finished:
		 */
		ReactorInput input = config.reactor_inputs.get(0);
		simulations[0] =  new CKEmulation(config, rt, input);
		simulations[0] = new RegularSimulationDecorator(input, simulations[0], semaphore);
		simulations[0] =  new FirstSimulationDecorator(simulations[0]);
		simulations[0].start();
		try {
			simulations[0].join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		AbstractChemkinRoutine routine = new ChemkinRoutine(config);
		routine.reactorDir = simulations[0].getReactorDir();
		//copy CKSolnList from working dir to specific reactor dir:
		Tools.copyFile(config.paths.getWorkingDir()+ChemkinConstants.CKSOLNLIST,simulations[0].getReactorDir()+ChemkinConstants.CKSOLNLIST);
		routine = new GetSolutionDecorator(routine);//decoration of parent chemkin routine:
		routine.executeCKRoutine();//execution
		
		try {
			simulations[0].getModelValue().setValue(new BufferedReader(new FileReader(new File(simulations[0].getReactorDir(),ChemkinConstants.CKCSVNAME))));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		//the postprocessed CKSoln.ckcsv file needs to be written to the parent directory (working directory)
		File excel_file = new File(simulations[0].getReactorDir(),ChemkinConstants.CKCSVNAME);
		File dummy = new File (config.paths.getOutputDir()+ChemkinConstants.CKCSVNAME+"_"+simulations[0].getReactorInput().filename+".csv");
		excel_file.renameTo(dummy);

		try {
			Tools.deleteFiles(simulations[0].getReactorDir(), ".zip");
			//delete complete reactorDir folder:
			Tools.deleteDir(new File(simulations[0].getReactorDir()));

		} catch(Exception exc){
			logger.error("Exception happened in CKEmulation run() method! - here's what I know: ", exc);
			//exc.printStackTrace();
			System.exit(-1);
		}
		/*
		 * Other simulations that will be parallelized.
		 */
		for (int i = 1; i < simulations.length; i++) {//start with 2nd simulation i = 1
			ReactorInput input_i = config.reactor_inputs.get(i);
			simulations[i] = new CKEmulation(config, rt, input_i);
			simulations[i] = new RegularSimulationDecorator(config.reactor_inputs.get(i), simulations[i], semaphore);


			//start a new thread that redirects to the run() method, which contains the  chemkin procedure
			simulations[i].start();
			logger.info("Thread "+i+" was started");
		}
		try{	
			for (int i = 1; i < simulations.length; i++){
				//wait until all CKEmulation threads are finished, before you start filling up the list:
				simulations[i].join();
			}
		}

		catch(InterruptedException e){
			//fall through
		}
		
		// run the GetSolution utility:
		for (int i = 1; i < simulations.length; i++) {//start with 2nd simulation i = 1
			routine = new ChemkinRoutine(config);
			routine.reactorDir = simulations[i].getReactorDir();
			//copy CKSolnList from working dir to specific reactor dir:
			Tools.copyFile(config.paths.getWorkingDir()+ChemkinConstants.CKSOLNLIST,simulations[i].getReactorDir()+ChemkinConstants.CKSOLNLIST);
			
			routine = new GetSolutionDecorator(routine);//decoration of parent chemkin routine:
			routine.executeCKRoutine();//execution
			
			try {
				simulations[i].getModelValue().setValue(new BufferedReader(new FileReader(new File(simulations[i].getReactorDir(),ChemkinConstants.CKCSVNAME))));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			//the postprocessed CKSoln.ckcsv file needs to be written to the parent directory (working directory)
			excel_file = new File(simulations[i].getReactorDir(),ChemkinConstants.CKCSVNAME);
			dummy = new File (config.paths.getOutputDir()+ChemkinConstants.CKCSVNAME+"_"+simulations[i].getReactorInput().filename+".csv");
			excel_file.renameTo(dummy);

			try {
				Tools.deleteFiles(simulations[i].getReactorDir(), ".zip");
				//delete complete reactorDir folder:
				Tools.deleteDir(new File(simulations[i].getReactorDir()));

			} catch(Exception exc){
				logger.error("Exception happened in CKEmulation run() method! - here's what I know: ", exc);
				//exc.printStackTrace();
				System.exit(-1);
			}
		}
		
		return simulations;
	}

}

