package chemkin_model;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import parsers.ConfigurationInput;

import readers.ReactorInput;
import util.ChemkinConstants;
import util.Paths;
import util.Semaphore;
import util.Tools;
import chemkin_wrappers.AbstractChemkinRoutine;
import chemkin_wrappers.ChemkinRoutine;
import chemkin_wrappers.GetSolutionDecorator;

/**
 * CKPackager is a type that bundles all executed CKEmulations into one data structure. 
 * It corresponds to the set of experiments that are executed and are structured in the same way
 * the reactor input files are provided in the input.xml file. 
 * @author nmvdewie
 *
 */
public class CKPackager extends AbstractCKPackager{

	//constructor for parameter optimization option:
	public CKPackager(){
		int length = ConfigurationInput.reactor_inputs.size();
		simulations = new AbstractCKEmulation[length];
	}

	@Override
	public AbstractCKEmulation []  runAllSimulations(){
		Semaphore semaphore = new Semaphore(ConfigurationInput.licenses.getValue()); 
		AbstractChemkinRoutine routine = new ChemkinRoutine();
		File excel_file = null;
		File dummy = null;
		
		for (int i = 0; i < simulations.length; i++) {//start with 2nd simulation i = 1
			ReactorInput input_i = ConfigurationInput.reactor_inputs.get(i);
			simulations[i] = new CKEmulation(input_i);
			simulations[i] = new RegularSimulationDecorator(ConfigurationInput.reactor_inputs.get(i), simulations[i], semaphore);


			//start a new thread that redirects to the run() method, which contains the  chemkin procedure
			simulations[i].start();
			logger.info("Thread "+i+" was started");
		}
		try{	
			for (int i = 0; i < simulations.length; i++){
				//wait until all CKEmulation threads are finished, before you start filling up the list:
				simulations[i].join();
			}
		}

		catch(InterruptedException e){
			//fall through
		}
		
		// run the GetSolution utility:
		for (int i = 0; i < simulations.length; i++) {//start with 2nd simulation i = 1
			routine = new ChemkinRoutine();
			routine.reactorDir = simulations[i].getReactorDir();
						
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
			dummy = new File (Paths.getOutputDir()+ChemkinConstants.CKCSVNAME+"_"+simulations[i].getReactorInput().filename+".csv");
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

