package parameter_estimation;
/**
 * Paths contains paths to directories, files that are important in the Parameter Estimation program<BR>
 * Paths serves as a supertype to Param_Est, CKPackager, Rosenbrock types
 * @author nmvdewie
 *
 */
public class Paths {
	protected String workingDir;
	protected String chemkinDir;
	protected String chem_inp;
	protected String [] reactor_inputs;
	protected int no_experiments;
	
	public Paths (String wd, String cd, String c_inp, String [] r_inp){
		workingDir = wd;
		chemkinDir = cd;
		chem_inp = c_inp;
		reactor_inputs = r_inp;
	}
}
