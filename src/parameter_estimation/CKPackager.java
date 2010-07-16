package parameter_estimation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CKPackager is a type that bundles all executed CKEmulations into one data structure. It corresponds to the set of experiments that are executed 
 * @author nmvdewie
 *
 */
public class CKPackager{
	
	Paths paths;
	public String [] reactorOutputs;
	
	
	public List<Map<String,Double>> listCKEmulations;
	public int noExperiments;
	boolean flagCKSolnList;
	boolean flagToExcel = false;

	//constructor for parameter optimization option:
	public CKPackager(Paths paths, boolean flag){
		this.paths = paths;
		noExperiments = paths.reactorInputs.length;
		flagCKSolnList = flag;
	}
	
	//constructor for toExcel option:
	public CKPackager(Paths paths, boolean flag, boolean toExcel){
		this(paths, flag);
		this.flagToExcel = toExcel;

	}
	
	public List<Map<String,Double>> getModelValues(){
		List<Map<String,Double>> list = new ArrayList<Map<String,Double>>();
		CKEmulation [] dummy = new CKEmulation[noExperiments];
		Runtime rt = Runtime.getRuntime();
		Semaphore semaphore = new Semaphore(paths.getNoLicenses());
		for (int i = 0; i < noExperiments; i++) {
			
			//only the first CK_emulation needs to create the CKSolnList file:
			if (i!=0){
				flagCKSolnList = false;
			}
					
			dummy[i] = new CKEmulation(paths, rt, paths.getReactorInputs()[i], flagCKSolnList, semaphore, flagToExcel);
			
			//start a new thread that redirects to the run() method, which contains the sequential chemkin procedure (chem -> CKReactorPlugFlow -> GetSolution ->...)
			dummy[i].start();
			System.out.println("Thread "+i+" was started");

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
			for (int j = 0; j < noExperiments; j++){
				//wait until all CKEmulation threads are finished, before you start filling up the list:
				dummy[j].join();
				
				Map<String, Double> m;
				m = dummy[j].getModelValue();
				list.add(m);	
			}
		} catch(InterruptedException e){
			//fall through
		}
		return list;
		
	}
}
