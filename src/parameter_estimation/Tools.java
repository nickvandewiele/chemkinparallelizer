package parameter_estimation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
/**
 * Tools provides static methods to move/delete one File or multiple files with the same extension
 * @author nmvdewie
 *
 */
public class Tools {
	static Logger logger = Logger.getLogger(ParameterEstimationDriver.logger.getName());
	/**
	 * moveFiles moves all files with extension e to the specified destination dir
	 * @param orig_dir
	 * @param dest_dir
	 * @param e
	 */
	protected static void moveFiles( String orig_dir, String dest_dir, String e ) {
	     ExtensionFilter filter = new ExtensionFilter(e);
	     File original_dir = new File(orig_dir);
	     String[] list = original_dir.list(filter);
	     if (list.length == 0) return;

	     for (int i = 0; i < list.length; i++) {
	       // Move file to new directory
	    	 File file = new File(list[i]);
	    	 boolean success = file.renameTo(new File(dest_dir, list[i]));
	    	 if (success) logger.info("File was successfully moved!"); 
	    	 else logger.debug("File was not successfully moved...");
	    	 //logger.info("File was successfully moved? " + success + "!");
	     }    
	}
	
	protected static void moveFile(String dest_dir, String filename){
		// File (or directory) to be moved
	    File file = new File(filename);
	    
	    // Move file to new directory
	    boolean success = file.renameTo(new File(dest_dir, file.getName()));
	    if (!success) {
	        // File was not successfully moved
	    }
	}
	/**
	  * from: http://forums.sun.com/thread.jspa?threadID=563148
	  * @param n
	  * @return
	  */
	 public static void deleteDir(File dir){
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
	 /**
		 * delete all files in directory d with extension e<BR>
		 * @param d
		 * @param e
		 */
		 public static void deleteFiles( String d, String e ) {
		     ExtensionFilter filter = new ExtensionFilter(e);
		     File dir = new File(d);
		     String[] list = dir.list(filter);
		     File file;
		     if (list.length == 0) return;

		     for (int i = 0; i < list.length; i++) {
		       file = new File(d + list[i]);
		       boolean isdeleted =   file.delete();
		       logger.info(file);
		       logger.info( "  deleted " + isdeleted);
		     }
		   }
		 /**
			 * convert 1D vector into 2D matrix: 
			 * @return
			 */
			public static double [][] convert1Dto2D(double [] vector, double[][] matrix){
				//convert 1D vector back to matrix [][] notation:
				double [][] matrixFilled = new double [matrix.length][matrix[0].length];
				int counter = 0;
				for (int i = 0; i < matrix.length; i++) {
					for (int j = 0; j < matrix[0].length; j++){
						matrixFilled[i][j] = vector[counter];
						counter++;
					}
				}
				return matrixFilled;
			}
			/**
			 * convert_massfractions cuts of the Mass_fraction_ part off of the species names string
			 * @param m
			 * @return
			 */
			public static Map<String,Double> cutOffMassFrac_(Map<String,Double> m){
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
			  * from: http://www.roseindia.net/java/beginners/CopyFile.shtml
			  * @param srFile
			  * @param dtFile
			  */
			 public static void copyFile(String srFile, String dtFile){
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
				      logger.info("File copied.");
				    }
				    catch(FileNotFoundException ex){
				      logger.error("Error while copying file",ex);
				      System.exit(0);
				    }
				    catch(IOException e){
				    	logger.error("Error while copying file",e);;      
				    }
				}
			 /**
				 * read_ckcsv should read the CKSoln.ckcsv file and retrieve data from it.<BR>
				 * Which data specifically is explained here below:<BR>
				 * 	<LI>the mole fraction of all species</LI>
				 * the values should be taken at the end point of the reactor, i.e. the last data value of each row in the .ckcsv file<BR>
				 * the data will be stored in a LinkedList, chosen for its flexibility<BR>
				 * @param reactorDir directory where the file to be read is located
				 * @param ckcsv_name filename of file
				 * @throws IOException
				 */
				public static Map<String, Double> readCkcsv (String reactorDir, String ckcsv_name) throws IOException {
					Map <String, Double> dummy = new HashMap<String, Double>();
					BufferedReader in = new BufferedReader(new FileReader(reactorDir+ckcsv_name));
					
					String temp;
					String [] st_temp;
					LinkedList<String> list_temp;
				
					/*
					 *  Looking for the String "Exit_mass_flow_rate" since the line right after this line,
					 *  will contain the first species' mass fractions
					 */
					list_temp = new LinkedList<String>();
					do {
						list_temp.clear();
						temp = in.readLine();
						st_temp = temp.split(", ");
						for (int i=0;i<st_temp.length;i++){
							list_temp.add(st_temp[i]);
						}
					} while (!(list_temp.get(0)).equals("Exit_mass_flow_rate"));

					
					/* read all species' mass fractions, number of species is unknown, until the String "Molecular_weight" is encountered,
					 * which implies that the end of the list with species' mass fractions has been reached.
					 */
					list_temp.clear();
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


					in.close();
					//convert to a HashMap with the real species names (cut of Mass_fraction_ or Mole_fraction_:
					dummy = cutOffMassFrac_(dummy);
					return dummy;
				}
				 /**
				  * getSpeciesNames retrieves the names of the species from the chemistry input file
				  * @return
				  * @throws IOException
				  */
				 public static List<String> getSpeciesNames(String workingDir, String chem_asu)throws IOException{
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
							logger.debug("Something went wrong with the species names parsing from the .asu file!!!");
							System.exit(-1);
						}
						//System.out.println(namesList.toString());
						
					return namesList;
				 }
					/**
					 * experiments_parser preprocesses the experimental data file into a easy-to-use format List<Map><String,Double>
					 * It reads the experimental data file. The format of this file (.csv) ought to be as follows:
					 * 	-first line states all the variable names, separated by commas
					 * 	-each of the following lines contain the experimental response variables, in the same order as the variable
					 *   names were declared
					 *  Each experiment is stored in a List with HashMaps as elements (see further)
					 *  The routines cuts each line in pieces, using the comma as separator
					 *  For each experiment, the variable name and the molar flow rate of each variable are put in a HashMap	 *   
					 * @return List of the experiments, with molar flowrates of the response variables
					 * @throws IOException
					 */
					public static List<Map<String, Double>> experimentsParser (String expName, int noExp)throws IOException{
						List<Map<String, Double>> exp = new ArrayList<Map<String, Double>>();
						try {
							//read experimental data file:
							BufferedReader in = new BufferedReader (new FileReader(expName));
							//read in species names on first line:
							String species_names = in.readLine();
							//System.out.println(species_names);
							String[] st_species = species_names.split(",");
							String dummy = in.readLine();
							//System.out.println(dummy);
							
							Map <String, Double> expMassFractions;
							while(dummy!=null){
								String[] st_dummy = dummy.split(",");
								expMassFractions = new HashMap <String, Double>();
								for (int j = 0; j < st_species.length; j++) {
									expMassFractions.put(st_species[j],Double.parseDouble(st_dummy[j]));	
								}
								exp.add(expMassFractions);
								//expMassFractions.clear();
								dummy = in.readLine();
								
							}
							if (exp.size()!= noExp){
								logger.debug("Experimental Database a different number of experiments than specified in INPUT file! Maybe check if .csv is created with redundand commas at the end...");
								System.exit(-1);
							}
							in.close();
						} catch(IOException e){
							logger.error("Something went wrong during the preprocessing of the experimental data file!",e);
							System.exit(-1);
						}
						if((exp.size()==noExp)){
							return exp;
						}
						else{
							logger.debug("Experiments database contains different no. of experiments as defined in main class!");
							System.exit(-1);
							return null;	
						}
						
					}							
					/**
					 * initial_guess returns the initial parameter guesses, found in the chem.inp file.
					 * It does so by reading the file, searching the key-String "REACTIONS	KJOULES/MOLE	MOLES"
					 * from that point on, every line is read and the 2nd and 4th subString is taken and stored in a List l
					 * The 2nd and 4th element correspond to A and Ea of the modified Arrhenius equation
					 * The List l is then converted to a double array and returned
					 * @return initial guesses for kinetic parameters, as double array 
					 * @throws IOException
					 */
					public static double[][] initialGuess (String workingDir, String chemInp, int [][] fixReactions) throws IOException{
						
						double[][] beta = new double[fixReactions.length][fixReactions[0].length];
						try {
							BufferedReader in = new BufferedReader(new FileReader(workingDir+chemInp));
							String dummy = in.readLine();
							
							//skip part of chem.inp about Elements, Species, Thermo
							boolean b = true;
							while(b){
								dummy = in.readLine();
								if (dummy.length() <= 8){
									b = true;
								}
								else if (dummy.substring(0,9).equals("REACTIONS")){
									b = false;
								}
								else {
									b = true;
								}
							}
					
				/**
				 * 
				 * 			A new approach is taken, providing guidelines to the user to adapt his/her reaction mechanism file according to 
				 * 			what is specified in the guidelines:
				 * 			GUIDELINES:
				 * 			-use <=> to denote a reversible reaction (not =)
				 * 			-separate kinetic parameters by a single space
				 * 			-use a single space after the definition of the elementary reaction, e.g. A+B<=>C+D 1e13 0.0 200.0		
				 */
							for (int i = 0; i < fixReactions.length; i++){
								dummy = in.readLine();
								String[] st_dummy = dummy.split("\\s");
								for (int j = 0; j < fixReactions[i].length; j++){
									//start with element at position 1 (not 0), because arrhenius parameters start at position 1!
									beta[i][j] = Double.parseDouble(st_dummy[j+1]);
								}
							}
							in.close();
							
						} catch (IOException e) {
							logger.error("Problem with obtaining initial parameter guesses!");
							System.exit(-1);
						}
						return beta;
					}	
}
