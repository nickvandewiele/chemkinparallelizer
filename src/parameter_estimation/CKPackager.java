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
	
	public String [] reactor_outputs;

	
	public List<Map<String,Double>> list_CKEmulations;
	public int no_experiments;
	
	//no_licenses sets the limiting number for the counting semaphore
	int no_licenses = 10;
	
	boolean flag_CKSolnList;
	boolean flag_toExcel = false;
	
	//use mass fractions in reactor solution data file by default:
	boolean flag_massfrac = true;

	//constructor for parameter optimization option, flag_massfrac remains false:
	public CKPackager(String wd, String cd, String c_inp, String [] r_i,  boolean flag){
		super(wd, cd, c_inp, r_i);
		no_experiments = r_i.length;
		flag_CKSolnList = flag;
	}
	
	//constructor for model predictions in mass fractions, used for parity plot mode:
	public CKPackager(String wd, String cd, String c_inp, String [] r_i,  boolean flag, boolean massfrac){
		this( wd, cd, c_inp, r_i, flag);
		this.flag_massfrac = massfrac;
	}
	//constructor for toExcel option:
	public CKPackager(String wd, String cd, String c_inp, String [] r_i,  boolean flag, boolean toExcel, boolean massfrac){
		this( wd, cd, c_inp, r_i, flag);
		this.flag_toExcel = toExcel;
		this.flag_massfrac = massfrac;
	}
	
	public List<Map<String,Double>> getModelValues(){
		List<Map<String,Double>> list = new ArrayList<Map<String,Double>>();
		CKEmulation [] dummy = new CKEmulation[no_experiments];
		Runtime rt = Runtime.getRuntime();

		Semaphore semaphore = new Semaphore(no_licenses);
		for (int i = 0; i < no_experiments; i++) {
			
			//only the first CK_emulation needs to create the CKSolnList file:
			if (i!=0){
				flag_CKSolnList = false;
			}
					
			dummy[i] = new CKEmulation(workingDir, chemkinDir, rt, chem_inp, reactor_inputs[i], flag_CKSolnList, semaphore, flag_toExcel, flag_massfrac);
			
			//start a new thread that redirects to the run() method, which contains the sequential chemkin procedure (chem -> CKReactorPlugFlow -> GetSolution ->...)
			dummy[i].start();
			System.out.println("Thread "+i+" was started");

			//wait to start other threads before the first thread, creating the CKSolnList.txt is completely finished:
			if (flag_CKSolnList){
				try{
					dummy[i].join();
					//finished
				}catch (InterruptedException e) {
				    // Thread was interrupted
				}
			}			
		}
		try{	
			for (int j = 0; j < no_experiments; j++){
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
