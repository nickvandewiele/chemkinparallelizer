package parameter_estimation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import chemkin_wrappers.AbstractChemkinRoutine;
import chemkin_wrappers.ChemkinRoutine;
import chemkin_wrappers.GetSolutionDecorator;
import readers.ReactorInput;
import readers.ReactorSetupInput;

public class RegularSimulationDecorator extends CKEmulationDecorator {

	//Semaphore that controls chemkin license check-in and check-outs:
	Semaphore semaphore;

	public RegularSimulationDecorator(ReactorInput reactorSetupInput,
			AbstractCKEmulation abstractCKEmulation, Semaphore semaphore2) {
		
		super.reactorInput = reactorSetupInput;
		super.simulation = abstractCKEmulation;
		this.semaphore = semaphore2;
	}

	@Override
	public void run() {

		semaphore.acquire();
		logger.info("license acquired!"+getReactorInput().filename);	
		//copy chemistry input to the reactorDir:
		Tools.copyFile(getConfig().paths.getWorkingDir()+getConfig().chemistry.getChemistryInput(),
				getReactorDir()+getConfig().chemistry.getChemistryInput());
		
		//chemkindataDTD:
		Tools.copyFile(getConfig().paths.getWorkingDir()+ChemkinConstants.CHEMKINDATADTD,getReactorDir()+ChemkinConstants.CHEMKINDATADTD);
/*
		//Input Folder with user-defined ROP:
		for(File filename: getConfig().paths.UDROPDir.listFiles()){//copy all files in this folder to reactor dir
			Tools.copyFile(getConfig().paths.UDROPDir.getAbsolutePath()+"/"+filename,
					reactorDir+filename);
		}
*/
		//instantiation of parent chemkin routine:
		AbstractChemkinRoutine routine = new ChemkinRoutine(getConfig(), getRuntime());
		routine.reactorDir = getReactorDir();
		routine.reactorOut = getReactorOut();
		routine.reactorSetup = getReactorInput().filename;
		CKEmulationFactory factory = new CKEmulationFactory(routine);
		routine = factory.createRoutine(getReactorInput().type);
		routine.executeCKRoutine();//execution
		//copy reactor diagnostics file to workingdir:
		Tools.copyFile(getReactorDir()+getReactorOut(),getConfig().paths.getWorkingDir()+getReactorOut());

		Tools.copyFile(getReactorDir()+ChemkinConstants.CKSOLNLIST,getConfig().paths.getWorkingDir()+ChemkinConstants.CKSOLNLIST);
	
		routine = new GetSolutionDecorator(routine);//decoration of parent chemkin routine:
		routine.executeCKRoutine();//execution
		
		//release the semaphore:
		semaphore.release();
		logger.info("license released!"+getReactorInput().filename);
		
		try {
			modelValue.setValue(new BufferedReader(new FileReader(new File(getReactorDir(),ChemkinConstants.CKCSVNAME))));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		//the postprocessed CKSoln.ckcsv file needs to be written to the parent directory (working directory)
		File excel_file = new File(getReactorDir(),ChemkinConstants.CKCSVNAME);
		File dummy = new File (getConfig().paths.getOutputDir()+ChemkinConstants.CKCSVNAME+"_"+getReactorInput().filename+".csv");
		excel_file.renameTo(dummy);

		
		try {
			Tools.deleteFiles(getReactorDir(), ".zip");
			//delete complete reactorDir folder:
			Tools.deleteDir(new File(getReactorDir()));

		} catch(Exception exc){
			logger.error("Exception happened in CKEmulation run() method! - here's what I know: ", exc);
			//exc.printStackTrace();
			System.exit(-1);
		}
		
	}




}