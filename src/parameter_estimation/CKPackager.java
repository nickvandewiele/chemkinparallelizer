package parameter_estimation;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * CKPackager is a type that bundles all executed CKEmulations into one data structure. It corresponds to the set of experiments that are executed 
 * @author nmvdewie
 *
 */
public class CKPackager extends Loggable{
	Paths paths;
	public String [] reactorOutputs;

	private ModelValues modelValues;

	public Chemistry chemistry;
	public Experiments experiments;
	public Licenses licenses;
	boolean flagCKSolnList;
	boolean flagToExcel;
	//basic constructor used in both other constructors:
	private CKPackager(Paths paths, Chemistry chemistry, Experiments experiments, Licenses licenses){
		this.paths = paths;
		this.chemistry = chemistry;
		this.experiments = experiments;
		this.licenses = licenses;
	}


	//constructor for parameter optimization option:
	public CKPackager(Paths paths, Chemistry chemistry, Experiments experiments, Licenses licenses, boolean flag){
		this(paths, chemistry, experiments, licenses);
		this.modelValues = new ModelValues();
		this.flagCKSolnList = flag;
		this.flagToExcel = false;
		runAllSimulations();
	}

	//constructor for toExcel option:
	public CKPackager(Paths paths, Chemistry chemistry, Experiments experiments, Licenses licenses, boolean flag, boolean toExcel){
		this(paths, chemistry, experiments, licenses);
		this.flagCKSolnList = flag;
		this.flagToExcel = toExcel;
		runAllSimulations();
	}

	public void runAllSimulations_Deprecated(){
		Runtime rt = Runtime.getRuntime();
		Semaphore semaphore = new Semaphore(licenses.getValue());
		CKEmulation [] dummy = new CKEmulation[experiments.getReactorInputCollector().getTotalNoExperiments()];

		for (int i = 0; i < experiments.getReactorInputCollector().getTotalNoExperiments(); i++) {
			try {
				//check whether it is an ignition delay experiment, to get the
				//ignition delay value instead of the effluent composition
				boolean flagIgnitionDelayExperiment = experiments.getReactorInputCollector().getFlagIgnitionDelays().get(experiments.getReactorInputCollector().getReactorInputs().get(i));
				//only the first CK_emulation needs to create the CKSolnList file:
				if (i!=0){
					flagCKSolnList = false;
				}
				dummy[i] = new CKEmulation(paths, chemistry, rt,
						experiments.getReactorInputCollector().getReactorInputs().get(i), semaphore,
						flagCKSolnList, flagToExcel, flagIgnitionDelayExperiment);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			//start a new thread that redirects to the run() method, which contains the sequential chemkin procedure (chem -> CKReactorPlugFlow -> GetSolution ->...)
			dummy[i].start();
			logger.info("Thread "+i+" was started");

			//wait to start other threads before the first thread, creating the CKSolnList.txt is completely finished:
			if (flagCKSolnList){
				try{
					dummy[i].join();
					//finished
				}catch (InterruptedException e) {
					// Thread was interrupted
				}
			}			
		}
		try{	
			for (int j = 0; j < experiments.getReactorInputCollector().getTotalNoExperiments(); j++){
				//wait until all CKEmulation threads are finished, before you start filling up the list:
				dummy[j].join();
				/**
				 * if the Excel Processing mode is used, the model values should not be stored.
				 */
				if(!flagToExcel){
					String reactorInput = experiments.getReactorInputCollector().getReactorInputs().get(j);
					/**
					 * depending on the type of experiment, the 'model value' will
					 * either be an ignition delay
					 * or an effluent composition
					 */
					if(experiments.getReactorInputCollector().getFlagIgnitionDelays().get(reactorInput)){
						LinkedList<Double> dummy2 = modelValues.getModelIgnitionValues();
						dummy2.add(dummy[j].getIgnitionValue());
						modelValues.setModelIgnitionValues(dummy2);
					}
					else{
						LinkedList<Map<String,Double>> dummy2 = modelValues.getModelEffluentValues();
						dummy2.add(dummy[j].getEffluentValue());
						modelValues.setModelEffluentValues(dummy2);
					}
				}
			}
		} catch(InterruptedException e){
			//fall through
		}
	}
	public void runAllSimulations(){
		runAllRegularSimulations();
		runAllIgntionDelaySimulations();
		runAllFlameSpeedSimulations();
	}

	public ModelValues getModelValues() {
		return modelValues;
	}
	
	public void runAllRegularSimulations(){
		Runtime rt = Runtime.getRuntime();
		Semaphore semaphore = new Semaphore(licenses.getValue());
		CKEmulation [] dummy = new CKEmulation[experiments.getReactorInputCollector().getNoRegularExperiments()];

		for (int i = 0; i < experiments.getReactorInputCollector().getNoRegularExperiments(); i++) {
			try {
				if (i!=0){
					flagCKSolnList = false;
				}
				boolean flagIgnitionDelayExperiment = false;
				boolean flagFlameSpeedExperiment = false;
				dummy[i] = new CKEmulation(paths, chemistry, rt, 
						experiments.getReactorInputCollector().getReactorInputs().get(i), semaphore,
						flagCKSolnList, flagToExcel,
						flagIgnitionDelayExperiment,flagFlameSpeedExperiment);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			//start a new thread that redirects to the run() method, which contains the sequential chemkin procedure (chem -> CKReactorPlugFlow -> GetSolution ->...)
			dummy[i].start();
			logger.info("Thread "+i+" was started");

			//wait to start other threads before the first thread, creating the CKSolnList.txt is completely finished:
			if (flagCKSolnList){
				try{
					dummy[i].join();
					//finished
				}catch (InterruptedException e) {
					// Thread was interrupted
				}
			}			
		}
		try{	
			for (int j = 0; j < experiments.getReactorInputCollector().getNoRegularExperiments(); j++){
				//wait until all CKEmulation threads are finished, before you start filling up the list:
				dummy[j].join();
				/**
				 * if the Excel Processing mode is used, the model values should not be stored.
				 */
				if(!flagToExcel){
					LinkedList<Map<String,Double>> dummy2 = modelValues.getModelEffluentValues();
					dummy2.add(dummy[j].getEffluentValue());
					modelValues.setModelEffluentValues(dummy2);

				}
			}
		} catch(InterruptedException e){
			//fall through
		}
	}
	public void runAllIgntionDelaySimulations(){
		Runtime rt = Runtime.getRuntime();
		Semaphore semaphore = new Semaphore(licenses.getValue());
		CKEmulation [] dummy = new CKEmulation[experiments.getReactorInputCollector().getNoIgnitionDelayExperiments()];

		for (int i = 0; i < experiments.getReactorInputCollector().getNoIgnitionDelayExperiments(); i++) {
			try {
				boolean flagIgnitionDelayExperiment = true;
				boolean flagFlameSpeedExperiment = false;
				//only the first CK_emulation needs to create the CKSolnList file:
				if (i!=0){
					flagCKSolnList = false;
				}
				dummy[i] = new CKEmulation(paths, chemistry, rt, 
						experiments.getReactorInputCollector().getReactorInputs().get(i),
						semaphore, flagCKSolnList, flagToExcel, 
						flagIgnitionDelayExperiment,flagFlameSpeedExperiment);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			//start a new thread that redirects to the run() method, which contains the sequential chemkin procedure (chem -> CKReactorPlugFlow -> GetSolution ->...)
			dummy[i].start();
			logger.info("Thread "+i+" was started");

			//wait to start other threads before the first thread, creating the CKSolnList.txt is completely finished:
			if (flagCKSolnList){
				try{
					dummy[i].join();
					//finished
				}catch (InterruptedException e) {
					// Thread was interrupted
				}
			}			
		}
		try{	
			for (int j = 0; j < experiments.getReactorInputCollector().getNoIgnitionDelayExperiments(); j++){
				//wait until all CKEmulation threads are finished, before you start filling up the list:
				dummy[j].join();
				/**
				 * if the Excel Processing mode is used, the model values should not be stored.
				 */
				if(!flagToExcel){
					LinkedList<Double> dummy2 = modelValues.getModelIgnitionValues();
					dummy2.add(dummy[j].getIgnitionValue());
					modelValues.setModelIgnitionValues(dummy2);
				}
			}
		} catch(InterruptedException e){
			//fall through
		}
	}
	public void runAllFlameSpeedSimulations(){
		Runtime rt = Runtime.getRuntime();
		Semaphore semaphore = new Semaphore(licenses.getValue());
		CKEmulation [] dummy = new CKEmulation[experiments.getReactorInputCollector().getNoFlameSpeedExperiments()];

		for (int i = 0; i < experiments.getReactorInputCollector().getNoFlameSpeedExperiments(); i++) {
			try {
				boolean flagFlameSpeedExperiment = true;
				boolean flagIgnitionDelayExperiment = false;
				//only the first CK_emulation needs to create the CKSolnList file:
				if (i!=0){
					flagCKSolnList = false;
				}
				dummy[i] = new CKEmulation(paths, chemistry, rt,
						experiments.getReactorInputCollector().getReactorInputs().get(i),
						semaphore, flagCKSolnList,
						flagToExcel,flagIgnitionDelayExperiment, flagFlameSpeedExperiment);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			//start a new thread that redirects to the run() method, which contains the sequential chemkin procedure (chem -> CKReactorPlugFlow -> GetSolution ->...)
			dummy[i].start();
			logger.info("Thread "+i+" was started");

			//wait to start other threads before the first thread, creating the CKSolnList.txt is completely finished:
			if (flagCKSolnList){
				try{
					dummy[i].join();
					//finished
				}catch (InterruptedException e) {
					// Thread was interrupted
				}
			}			
		}
		try{	
			for (int j = 0; j < experiments.getReactorInputCollector().getNoFlameSpeedExperiments(); j++){
				//wait until all CKEmulation threads are finished, before you start filling up the list:
				dummy[j].join();
				/**
				 * if the Excel Processing mode is used, the model values should not be stored.
				 */
				if(!flagToExcel){
					LinkedList<Double> dummy2 = modelValues.getModelFlameSpeedValues();
					dummy2.add(dummy[j].getFlameSpeedValue());
					modelValues.setModelFlameSpeedValues(dummy2);
				}
			}
		} catch(InterruptedException e){
			//fall through
		}
	
	}
	
	
	/**
	 * ####################
	 * GETTERS AND SETTERS:
	 * ####################
	 */
	
	/**
	 * @category setter
	 * @param modelValues
	 */
	public void setModelValues(ModelValues modelValues) {
		this.modelValues = modelValues;
	}
}

