package parameter_estimation;

import java.io.File;

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
	protected String outputDir;
	
	public Paths (String wd, String cd, String c_inp, String [] r_inp){
		workingDir = wd;
		chemkinDir = cd;
		chem_inp = c_inp;
		reactor_inputs = r_inp;
		outputDir = workingDir+"output/";
		
	}
	protected void createOutputDir (){
		boolean temp = new File(outputDir).mkdir();
		if(!temp){
			System.out.println("Creation of output directory failed!");
			System.exit(-1);
		}
	}
	/**
	 * moveFiles moves all files with extension e to the specified destination dir
	 * @param orig_dir
	 * @param dest_dir
	 * @param e
	 */
	protected void moveFiles( String orig_dir, String dest_dir, String e ) {
	     ExtensionFilter filter = new ExtensionFilter(e);
	     File original_dir = new File(orig_dir);
	     String[] list = original_dir.list(filter);
	     if (list.length == 0) return;

	     for (int i = 0; i < list.length; i++) {
	       // Move file to new directory
	    	 File file = new File(list[i]);
	    	 boolean success = file.renameTo(new File(dest_dir, list[i]));
	    	 System.out.println( "File was successfully moved? " + success + "!");
	     }    
	}
	protected void moveOutputFiles (){
		moveFiles(workingDir, outputDir, ".out");
		moveFiles(workingDir, outputDir, ".asu");
		moveFiles(workingDir, outputDir, ".input");
		moveFiles(workingDir, outputDir, ".asc");
		moveFile(outputDir,"CKSolnList.txt");
		
	}
	protected void moveFile(String dest_dir, String filename){
		// File (or directory) to be moved
	    File file = new File(filename);
	    
	    // Move file to new directory
	    boolean success = file.renameTo(new File(dest_dir, file.getName()));
	    if (!success) {
	        // File was not successfully moved
	    }
	}
}
