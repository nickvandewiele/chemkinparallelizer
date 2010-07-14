package parameter_estimation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CKPackager is a type that bundles all executed CKEmulations into one data structure. It corresponds to the set of experiments that are executed 
 * @author nmvdewie
 *
 */
public class CKPackager extends Paths{
	
	public String [] reactorOutputs;

	
	public List<Map<String,Double>> listCKEmulations;
	public int noExperiments;
	
	boolean flagCKSolnList;
	boolean flagToExcel = false;
	
	//use mass fractions in reactor solution data file by default:
	boolean flagMassfrac = true;

	//constructor for parameter optimization option, flag_massfrac remains false:
	public CKPackager(String wd, String cd, String c_inp, String [] r_i, int no_lic, boolean flag){
		super(wd, cd, c_inp, r_i, no_lic);
		noExperiments = r_i.length;
		flagCKSolnList = flag;
	}
	
	//constructor for model predictions in mass fractions, used for parity plot mode:
	public CKPackager(String wd, String cd, String c_inp, String [] r_i, int no_lic,  boolean flag, boolean massfrac){
		this( wd, cd, c_inp, r_i, no_lic, flag);
		this.flagMassfrac = massfrac;
	}
	//constructor for toExcel option:
	public CKPackager(String wd, String cd, String c_inp, String [] r_i, int no_lic,  boolean flag, boolean toExcel, boolean massfrac){
		this( wd, cd, c_inp, r_i, no_lic, flag);
		this.flagToExcel = toExcel;
		this.flagMassfrac = massfrac;
	}
	
	public List<Map<String,Double>> getModelValues(){
		List<Map<String,Double>> list = new ArrayList<Map<String,Double>>();
		CKEmulation [] dummy = new CKEmulation[noExperiments];
		Runtime rt = Runtime.getRuntime();
		Semaphore semaphore = new Semaphore(noLicenses);
		for (int i = 0; i < noExperiments; i++) {
			
			//only the first CK_emulation needs to create the CKSolnList file:
			if (i!=0){
				flagCKSolnList = false;
			}
					
			dummy[i] = new CKEmulation(workingDir, chemkinDir, outputDir, rt, chem_inp, reactorInputs[i], flagCKSolnList, semaphore, flagToExcel, flagMassfrac);
			
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
