package parameter_estimation;

import java.io.*;
import java.util.*;
/**
 * CKEmulation is designed as a Thread type, implying that multiple CKEmulations can be initiated, allowing multithreading and possible speed-up<BR>
 * CKEmulation can call several Chemkin routines: Chem, CKReactorPlugFlow, GetSolution, GetSolnTranspose depending on the task to be executed<BR>
 * In order to cope with a limited number of Chemkin licenses, a counting Semaphore type is used to keep track of the number of licenses in use<BR>
 *  
 * @author nmvdewie
 *
 */
public class CKEmulation extends Thread{

	public String workingDir;
	public String chemkinDir;
	private String binDir;
	private String reactorDir;
	private String outputDir;
	
	private Runtime r;
	
	private String chem_inp;
	private String chem_out = "chem.out";
	private String chem_asc = "chem.asc";
	private String chem_asu = "chem.asu";

	// input file to CKPreProcess routine:
	private String preProc_inp = "CKPreProc_template.input";
	
	public String reactor_setup;
	public String reactor_out;
	
	public String CKSolnList = "CKSolnList.txt";
	public String xml = "XMLdata.zip";
	public String ckcsv_name = "CKSoln.ckcsv";
	
	public int no_points;
	public double mw_mean;
	public double mass_flow;
	public double molar_flow;

	//species_fractions will be mol fractions or mass fractions depending on the flag massfrac. Anywho, the fractions are directly read through read_ckcsv
	public Map<String,Double> species_fractions;
	public Map<String,Double> species_molar_rates;
	
	//'first' is flag that tells you if the CKSolnList needs to be constructed or not.
	boolean first;
	
	//'excel' tells you if the excel file (transposed CKSoln.ckcsv) needs to be created:
	boolean flag_excel;
	
	//flag_massfrac tells you if the postprocessing files will use mass fractions (instead of the standard mole fractions)
	boolean flag_massfrac;
	
	//Semaphore that controls chemkin license check-in and check-outs:
	Semaphore semaphore;
	
	//CONSTRUCTORS:
	//constructor for checking validity of chemistry input file:
	public CKEmulation(String workingDir, String chemkinDir, String outputDir, Runtime runtime, String c_i){
		this.workingDir = workingDir;
		this.chemkinDir = chemkinDir;
		this.outputDir = outputDir;
		binDir = this.chemkinDir+"/bin/";
		r = runtime;
		chem_inp = c_i;
	}
	
	//constructor for creating CKSolnList.txt
	public CKEmulation(String workingDir, String chemkinDir, String outputDir, Runtime runtime, String c_i, String rs, boolean f){
		this( workingDir,  chemkinDir,  outputDir, runtime,  c_i);
		reactor_setup = rs;
		first = f;
	}
	
	//constructor for running 'classical' Chemkin routines
	public CKEmulation(String workingDir, String chemkinDir, String outputDir, Runtime runtime, String c_i, String rs, boolean f, Semaphore s, boolean excel, boolean massfrac){
		this(workingDir, chemkinDir, outputDir, runtime,  c_i,  rs,  f);
		int length = rs.length();
		reactor_out = rs.substring(0,(length-4))+".out";
	
		this.flag_excel = excel;
		this.flag_massfrac = massfrac;
		semaphore = s;
		
		this.reactorDir = workingDir+"temp_ "+rs.substring(0,(length-4))+"/";
		boolean temp = new File(reactorDir).mkdir();
		if(!temp){
			System.out.println("Creation of reactor directory failed!");
			System.exit(-1);
		}
		
	}
	/**
	 * run() is the method that will be executed when Thread.start() is executed. Its argument list is void (mandatory I think).
	 */
	public void run(){
		try {
			semaphore.acquire();
			
			System.out.println("license acquired!"+reactor_setup);
			
			//copy chem.inp to the reactorDir:
			copyFile(workingDir+chem_inp,reactorDir+chem_inp);
			copyFile(workingDir+reactor_setup,reactorDir+reactor_setup);
			copyFile(workingDir+"chemkindata.dtd",reactorDir+"chemkindata.dtd");
			callChem();	
			//call_PreProcess();
			callReactor();
			
			//copy reactor diagnostics file to workingdir:
			copyFile(reactorDir+reactor_out,workingDir+reactor_out);
			
			//boolean first: if first time: create and adapt CKSolnList.txt file
			if (first){
				createSolnList();
				setSolnList();
				//copy the newly created CKSolnList to the workingDir so that it can be picked up by the other CK_emulations:
				copyFile(reactorDir+CKSolnList,workingDir+CKSolnList);
			}
			else {
				//copy the CKSolnList to the reactorDir
				copyFile(workingDir+CKSolnList,reactorDir+CKSolnList);
			}
			callGetSol(flag_massfrac);
			
			// if flag_excel = false: retrieve species fractions from the CKSoln.ckcsv file and continue:
			if (!flag_excel){
				//String name_data_ckcsv = "data_ckcsv"+j;
				Map<String,Double> temp = readCkcsv();
				
				//convert to a HashMap with the real species names (cut of Mass_fraction_ or Mole_fraction_:
				species_fractions = convertMassfractions(temp);
							
			}
			
			//if flag_excel = true: the postprocessed CKSoln.ckcsv file needs to be written to the parent directory (working directory)
			if (flag_excel){
				File excel_file = new File(reactorDir+ckcsv_name);
				File dummy = new File (outputDir+ckcsv_name+"_"+reactor_setup+".csv");
				excel_file.renameTo(dummy);
			}
			//delete complete reactorDir folder:
			deleteDir(new File(reactorDir));
/*			
			//wait for semaphore release until directory is completely deleted, don't know if this works...
			while(new File(reactorDir).exists()){
			}
*/			
			//when all Chemkin routines are finished, release the semaphore:
 			semaphore.release();
 			
			System.out.println("license released!"+reactor_setup);
			
		} catch(Exception exc){
			System.out.println("Exception happened in CKEmulation run() method! - here's what I know: ");
			exc.printStackTrace();
			System.exit(-1);
			}
	}
	/**
	 * calls the .bat file that sets environment variables for proper use of future Chemkin calls<BR>
	 * @throws Exception
	 */
	public void callBat () throws IOException, InterruptedException {
		String [] setup_environment = {binDir+"run_chemkin_env_setup.bat"};
		executeCKRoutine(setup_environment);
	}
	
	/**
	 * call the Chemkin preprocessor chem and produces the linking file (.asc)
	 * @throws Exception
	 */
	public void callChem () throws IOException, InterruptedException {
		String [] preprocess = {binDir+"chem","-i",reactorDir+chem_inp,"-o",reactorDir+chem_out,"-c",reactorDir+chem_asc};
		executeCKRoutine(preprocess);
	}
	public void call_PreProcess() throws IOException, InterruptedException {
		setCKPreProcess_Input();
		String [] preprocess = {binDir+"CKPreProcess","-i",workingDir+preProc_inp};
		executeCKRoutine(preprocess);
	}
	/**
	 * call a Chemkin reactor model (ic: CKReactorPlugFlow) executable	
	 * @throws Exception
	 */
	public void callReactor () throws IOException, InterruptedException {
		String [] name_input_PFR = {binDir+"CKReactorPlugFlow","-i",reactorDir+reactor_setup,"-o",reactorDir+reactor_out};
		executeCKRoutine(name_input_PFR, new File(reactorDir));
	}
	
	/**
	 * processes the xmldata.zip file using Chemkin GetSolution executable
	 * @param flag_massfrac if true: GetSolution prints mass fractions instead of mole fractions (used for Excel Postprocessing and Parity mode)
	 * @throws Exception
	 */
	public  void callGetSol (boolean flag_massfrac) throws IOException, InterruptedException {
		//String abbrev_path = cd+"data/abbreviations.csv";
		if (flag_massfrac){
			String [] progGetSol = {binDir+"GetSolution","-nosen","-norop","-mass",reactorDir+xml};
			executeCKRoutine(progGetSol, new File (reactorDir));
		}
		else {
			String [] progGetSol = {binDir+"GetSolution","-nosen","-norop",reactorDir+xml};
			executeCKRoutine(progGetSol, new File (reactorDir));
		}
		deleteFiles(reactorDir, ".zip");
	}
	/**
	 * calls the Chemkin CKSolnTranspose executable
	 * @throws Exception
	 */
	public void callTranspose () throws Exception {
		String [] progTranspose = {binDir+"CKSolnTranspose",reactorDir+ckcsv_name};
		executeCKRoutine(progTranspose);

	}
	/**
	 * read_ckcsv should read the CKSoln.ckcsv file and retrieve data from it.<BR>
	 * Which data specifically is explained here below:<BR>
	 * 	<LI>the total exit mass flow rate</LI>
	 * 	<LI>the mole fraction of all species</LI>
	 * 	<LI>the mean molecular weight</LI>
	 * the values should be taken at the end point of the reactor, i.e. the last data value of each row in the .ckcsv file<BR>
	 * the data will be stored in a LinkedList, chosen for its flexibility<BR>
	 * @throws IOException
	 */
	private Map<String, Double> readCkcsv () throws IOException {
		Map <String, Double> dummy= new HashMap<String, Double>();
		BufferedReader in = new BufferedReader(new FileReader(reactorDir+ckcsv_name));
		
		String temp =in.readLine();
		String [] st_temp = temp.split(", ");
		LinkedList<String> list_temp = new LinkedList<String>();
		for (int i=0;i<st_temp.length;i++){
			list_temp.add(st_temp[i]);
		}
		
		no_points = Integer.parseInt(list_temp.get(2));
		//System.out.println("no_points: "+no_points);
		
		//second line contains distance
		temp =in.readLine();
		//position of end point values is retrieved from size of LinkedList of the distance:

		// read total mass flow rate:
		list_temp = new LinkedList<String>();
		do {
			list_temp.clear();
			temp = in.readLine();
			st_temp = temp.split(", ");
			for (int i=0;i<st_temp.length;i++){
				list_temp.add(st_temp[i]);
			}
		} while (!(list_temp.get(0)).equals("Exit_mass_flow_rate"));
		mass_flow = Double.parseDouble(list_temp.get(list_temp.size()-1));
		
		//read all species' mole fractions, number of species is unknown
		list_temp = new LinkedList<String>();
		do {
			list_temp.clear();
			temp =in.readLine();
			st_temp = temp.split(", ");
			for (int i=0;i<st_temp.length;i++){
				list_temp.add(st_temp[i]);
			}
			if(!(list_temp.get(0)).equals("Molecular_weight")){				
				dummy.put((String)list_temp.get(0), Double.parseDouble(list_temp.get(list_temp.size()-1)));
				
			}
		} while (!(list_temp.get(0)).equals("Molecular_weight"));

		// read mean molecular weight, supposedly the last in.readLine() read in the last loop.
		mw_mean = Double.parseDouble(list_temp.get(list_temp.size()-1));
		molar_flow = mass_flow / mw_mean;
		in.close();
		return dummy;
	}
	/**
	 * convert_massfractions cuts of the Mass_fraction_ part off of the species names string
	 * @param m
	 * @return
	 */
	private Map<String,Double> convertMassfractions(Map<String,Double> m){
		Map<String, Double> dummy = new HashMap<String, Double> ();
		//loop through keys
		for ( String s : m.keySet()){
				//omit substring "Mass_fraction_" from key, i.e. take substring starting from character at position 14
				String dummy_name = s.substring(14);
				Double dummy_value = m.get(s);
				dummy.put(dummy_name, dummy_value);
		}
		return dummy;
	}	
	/**
	 * convert_molrates takes the HashMap constructed after reading the CKSoln.ckcsv as a argument and converts this HashMap<BR>
	 * into a new HashMap with [variable name, molar flowrates] as the elements<BR>
	 * @param m the HashMap generated from the read_ckcsv method containing species molfractions
	 * @return
	 */
	private Map<String,Double> convertMolrates(Map<String,Double> m){
		Map<String, Double> dummy = new HashMap<String, Double> ();
		//loop through keys
		for ( String s : m.keySet()){
				//omit substring "Mole_fraction_" from key, i.e. take substring starting from character at position 14
				//String dummy_name = s.substring(14);
				String dummy_name = s;
				//calculate species molar flow rate as total molar flow rate * species molar fraction
				Double dummy_value = m.get(s) * molar_flow;
				dummy.put(dummy_name, dummy_value);
		}
		return dummy;
	}
	/**
	 * delete all files in directory d with extension e<BR>
	 * @param d
	 * @param e
	 */
	 private void deleteFiles( String d, String e ) {
	     ExtensionFilter filter = new ExtensionFilter(e);
	     File dir = new File(d);
	     String[] list = dir.list(filter);
	     File file;
	     if (list.length == 0) return;

	     for (int i = 0; i < list.length; i++) {
	       file = new File(d + list[i]);
	       boolean isdeleted =   file.delete();
	       System.out.print(file);
	       System.out.println( "  deleted " + isdeleted);
	     }
	   }
	 /**
	  * checkChemInput does a preliminary check of the initial chemistry output file to verify if no errors are present.<BR>
	  * It therefore calls the Chemkin preprocessor which produces the output file<BR>
	  * This output file is read, and the String  " NO ERRORS FOUND ON INPUT: " is sought.<BR>
	  * If this String is not present, System.exit(-1) is called<BR>
	  */
	 public void checkChemInput(){
		 try {
			//create CKPreprocess.input file with directions to chem_inp, etc
			setCKPreProcess_Input();
			String [] preprocess = {binDir+"CKPreProcess","-i",workingDir+preProc_inp};
			executeCKRoutine(preprocess);
				
				//read the produced chem.out (path_output) file, and check if it contains error messages:
				BufferedReader in = new BufferedReader(new FileReader(workingDir+chem_out));
				String dummy = null;
				boolean flag = true;
				try {
					while(flag){
						dummy = in.readLine();
						if (dummy.equals(" NO ERRORS FOUND ON INPUT: ")){
							flag = false;
						}
					}
					in.close();
					if(!flag){
						System.out.println("Initial chemistry input file contains no errors. Proceed to parameter estimation!");
					}
					
				} catch(Exception e){
					System.out.println("Initial chemistry input file contains errors. Revision required!");
					System.exit(-1);
				}
		 }catch(Exception exc){
					System.out.println("exception happened - here's what I know: ");
					exc.printStackTrace();
					System.exit(-1);
		 }
	 }
	 /**
	  * createSolnList creates the CKSolnList.txt file by calling the "GetSolution -listonly" routine<BR>
	  * @throws Exception
	  */
	 public void createSolnList()throws Exception{
		String [] progGetList = {binDir+"GetSolution","-listonly",reactorDir+xml};
		executeCKRoutine(progGetList, new File(reactorDir));	
	 }
	 /**
	  * Set the SolnList.txt to the desired format:<BR>
	  * <LI>lines with # are left untouched</LI>
	  * <LI>no information whatsoever for all variables, except species, MW, exit_mass_flow_rate</LI>
	  * <LI>no sensitivity info for species, MW, exit_mass_flow_rate</LI>
	  * <LI>set UNIT of distance to (cm)</LI>
	  * <LI>all species mole fractions are reported, also those with negative fractions: FILTER MIN</LI>
	  * @throws Exception
	  */
	 public void setSolnList()throws Exception{
		 BufferedReader in = new BufferedReader(new FileReader(reactorDir+CKSolnList));
		 String temp = "tempList.txt";
		 PrintWriter out = new PrintWriter(new FileWriter(reactorDir+temp));
		 try{
			 String dummy = null;
			 dummy = in.readLine();
			 List<String> speciesNames = getSpeciesNames();
			 //if a comment line (starts with char '#') is read, just copy it to output file
			 while(!dummy.equals(null)){
				 //if a comment line (#) or a blank line is read, just copy and continue
				 if (dummy.trim().length()==0){
					 out.println(dummy);
					 dummy = in.readLine();
					 //System.out.println(dummy);
				 }
				 else if (dummy.charAt(0)=='#'||(dummy.trim().length()==0)) {
					 out.println(dummy);
					 dummy = in.readLine();
					 //System.out.println(dummy);
				 }
				 
				 else {
					 //separator are TWO spaces, not just one space!
					 String[] st_dummy = dummy.split("  ");
					 //only species variables and molecular weight variable are reported:
					 if (st_dummy[0].equals("VARIABLE")){
						 //check if the 2nd keyword matches "molecular weight":
						 if (st_dummy[1].equals("molecular_weight")){
							//no sensitivity info for molecular weight variable
							 st_dummy[4]="0";
						 }
						 //check if the 2nd keyword matches "exit_mass_flow_rate":
						 else if(st_dummy[1].equals("exit_mass_flow_rate")){
							 st_dummy[4]="0";
						 }
						 //check if 2nd keyword is one of the species names:
						 else if(speciesNames.contains(st_dummy[1])){
							 //no sensitivity info for species: set last number in the line to zero
							 st_dummy[4]="0";
						 }
						 //the rest of the variables are set to zero and will not be reported in the .ckcsv file
						 else {
							 //st_dummy[3] is standard equal to zero
							 st_dummy[2]="0";
							 st_dummy[4]="0";
							 
						 }
					 }
					 //set UNIT of Distance to m instead of cm:
					 else if(st_dummy[0].equals("UNIT")){
						 if (st_dummy[1].equals("Distance")){
							 st_dummy[2]="(cm)";
						 }
					 }
					 
					 //make sure even negative mole fractions are printed:
					 else if(st_dummy[0].equals("FILTER")){
						 if (st_dummy[1].equals("MIN")){
							 st_dummy[2]="-1.0";
						 }
					 }
					 
					 //concatenate String array back to its original form:
					 String dummy_out = st_dummy[0];
					 //add double spaces between Strings again:
					 for(int i=1;i<st_dummy.length;i++){
						 dummy_out += "  "+st_dummy[i];
					 }
					 
					 out.println(dummy_out);
					 dummy = in.readLine();
					 //System.out.println(dummy);
				 }
			 } 
		}catch (Exception e){//do nothing: e catches the end of the file exception
			
			}
		in.close();
		out.close();
		File old = new File(reactorDir+CKSolnList);
		old.delete();
		File f_temp = new File(reactorDir+temp);
		f_temp.renameTo(new File(reactorDir+CKSolnList));
	}
	 
	 public void executeCKRoutine (String [] CKCommand) throws IOException, InterruptedException{
		 String s = null;		
		 Process p = r.exec(CKCommand);
			
		 BufferedReader stdInput_p = new BufferedReader(new InputStreamReader(p.getInputStream()));
		 BufferedReader stdError_p = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	    
		// read the output from the command
	        System.out.println("Here is the standard output of the command:\n");
	        while ((s = stdInput_p.readLine()) != null) {
	            System.out.println(s);
	        }
	        stdInput_p.close();
	   // read any errors from the attempted command
	        System.out.println("Here is the standard error of the command (if any):\n");
	        while ((s = stdError_p.readLine()) != null) {
	            System.out.println(s);
	        }
	        stdError_p.close();
	        
			p.waitFor();
	        p.destroy();
			//System.out.println("Setup finished");		
	}
	 /**
	  * this routine overloads the standard execute_CKRoutine with a specified working directory, different from the standard working directory
	  * @param CKCommand
	  * @param working_directory
	  * @throws IOException
	  * @throws InterruptedException
	  */
	 public void executeCKRoutine (String [] CKCommand, File working_directory) throws IOException, InterruptedException{
		 String s = null;
		 String [] environment = null;
		 Process p = r.exec(CKCommand, environment, working_directory);
			
		 BufferedReader stdInput_p = new BufferedReader(new InputStreamReader(p.getInputStream()));
		 BufferedReader stdError_p = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	    
		// read the output from the command
	        System.out.println("Here is the standard output of the command:\n");
	        while ((s = stdInput_p.readLine()) != null) {
	            System.out.println(s);
	        }
	        stdInput_p.close();
	   // read any errors from the attempted command
	        System.out.println("Here is the standard error of the command (if any):\n");
	        while ((s = stdError_p.readLine()) != null) {
	            System.out.println(s);
	        }
	        stdError_p.close();
	        
			p.waitFor();
	        p.destroy();
			//System.out.println("Setup finished");		
	}
	 /**
	  * getSpeciesNames retrieves the names of the species from the chemistry input file
	  * @return
	  * @throws IOException
	  */
	 
	 public Map<String,Double> getModelValue(){
		 /**
		  * TODO: this needs to be settled in a better (less patchy) way!
		  */
		 if (flag_massfrac){
			 return species_fractions;
		 }
		 else {
			 return species_molar_rates;
		 }

	 }

	 public List<String> getSpeciesNames()throws IOException{
		 BufferedReader in = new BufferedReader (new FileReader(workingDir+chem_asu));
			List<String> namesList = new ArrayList<String>();
			
			//first line contains number of species:
			int no_species = Integer.parseInt(in.readLine());
			
			String dummy = in.readLine();
			//first part of dummy contains: species=
			String dummy_speciesEq = dummy.substring(0, 8);
			//System.out.println(dummy_speciesEq);
			
			//rest of dummy contains species name and mw:
			String otherEnd = dummy.substring(8,dummy.length());
			//System.out.println(otherEnd);
			int index_mw = otherEnd.indexOf("mw=");
			//System.out.println(index_mw);
			String species_name = otherEnd.substring(0, index_mw-1).trim();
			//System.out.println(species_name);
			
			while(dummy_speciesEq.equals("species=")){
				namesList.add(species_name);
				dummy = in.readLine();
				if(dummy.length()>=8) {
					dummy_speciesEq = dummy.substring(0, 8);
					//System.out.println(dummy_speciesEq);
					
					//rest of dummy contains species name and mw:
					otherEnd = dummy.substring(8,dummy.length());
					//System.out.println(otherEnd);
					index_mw = otherEnd.indexOf("mw=");
					//System.out.println(index_mw);
					species_name = otherEnd.substring(0, index_mw-1).trim();
					//System.out.println(species_name);
				}
				else {
					break;
				}
			}
			
				
			in.close();
			if(no_species != namesList.size()){
				System.out.println("Something went wrong with the species names parsing from the .asu file!!!");
				System.exit(-1);
			}
			//System.out.println(namesList.toString());
			
		return namesList;
	 }
	 /**
	  * from: http://www.roseindia.net/java/beginners/CopyFile.shtml
	  * @param srFile
	  * @param dtFile
	  */
	 private void copyFile(String srFile, String dtFile){
		    try{
		      File f1 = new File(srFile);
		      File f2 = new File(dtFile);
		      InputStream in = new FileInputStream(f1);
		      OutputStream out = new FileOutputStream(f2);

		      byte[] buf = new byte[1024];
		      int len;
		      while ((len = in.read(buf)) > 0){
		        out.write(buf, 0, len);
		      }
		      in.close();
		      out.close();
		      System.out.println("File copied.");
		    }
		    catch(FileNotFoundException ex){
		      System.out.println(ex.getMessage() + " in the specified directory.");
		      System.exit(0);
		    }
		    catch(IOException e){
		      System.out.println(e.getMessage());      
		    }
		}
	 
	 /**
	  * from: http://forums.sun.com/thread.jspa?threadID=563148
	  * @param n
	  * @return
	  */
	 private void deleteDir(File dir){
		   Stack<File> dirStack = new Stack<File>();
		   dirStack.push(dir);
		  
		   boolean containsSubFolder;
		   while(!dirStack.isEmpty()){
		      File currDir = dirStack.peek();
		      containsSubFolder = false;
		    
		      String[] fileArray = currDir.list();
		      for(int i=0; i<fileArray.length; i++){
		         String fileName = currDir.getAbsolutePath() + File.separator + fileArray[i];
		         File file = new File(fileName);
		         if(file.isDirectory()){
		            dirStack.push(file);
		            containsSubFolder = true;
		         }else{
		            file.delete(); //delete file
		         }       
		      }
		 
		      if(!containsSubFolder){
		         dirStack.pop(); //remove curr dir from stack
		         currDir.delete(); //delete curr dir
		      }
		   }
		}
	 public void setCKPreProcess_Input()throws IOException, InterruptedException{
		 //in windows: user.dir needs to be followed by "\", in *nix by "/"... 
		 String osname = System.getProperty("os.name");
		 if (osname.equals("Linux")){
			 PrintWriter out = new PrintWriter(new FileWriter(workingDir+preProc_inp));
			 out.println("IN_CHEM_INPUT="+System.getProperty("user.dir")+"/"+chem_inp);
			 out.println("OUT_CHEM_OUTPUT="+System.getProperty("user.dir")+"/"+chem_out);
			 out.println("OUT_CHEM_ASC="+System.getProperty("user.dir")+"/"+chem_asc);
			 out.println("OUT_CHEM_SPECIES="+System.getProperty("user.dir")+"/"+chem_asu);
			 out.close();
		 }
		 else {
			 PrintWriter out = new PrintWriter(new FileWriter(workingDir+preProc_inp));
		 out.println("IN_CHEM_INPUT="+System.getProperty("user.dir")+"\\"+chem_inp);
		 out.println("OUT_CHEM_OUTPUT="+System.getProperty("user.dir")+"\\"+chem_out);
		 out.println("OUT_CHEM_ASC="+System.getProperty("user.dir")+"\\"+chem_asc);
		 out.println("OUT_CHEM_SPECIES="+System.getProperty("user.dir")+"\\"+chem_asu);
		 out.close();
		 }
	 }
}


	

