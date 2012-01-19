package parameter_estimation;
import parsers.ConfigurationInput;
import readers.ReactorInput;
import readers.ReactorSetupInput;

/**
 * CKPackager is a type that bundles all executed CKEmulations into one data structure. It corresponds to the set of experiments that are executed 
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
		Semaphore semaphore = new Semaphore(config.licenses.getValue()); 
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
		/*
		 * Other simulations that will be parallelized.
		 */
		for (int i = 1; i < simulations.length; i++) {//start with 2nd simulation i = 1
			ReactorInput input_i = config.reactor_inputs.get(i);
			simulations[i] = new CKEmulation(config, rt, input_i);
			simulations[i] = new RegularSimulationDecorator(config.reactor_inputs.get(i), simulations[i], semaphore);


			//start a new thread that redirects to the run() method, which contains the sequential chemkin procedure (chem -> CKReactorPlugFlow -> GetSolution ->...)
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
		return simulations;
	}

}

