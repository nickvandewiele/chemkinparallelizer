package parameter_estimation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import optimization.Parameters2D;

/**
 * type that groups information on chemistry of the system
 * @author nmvdewie
 *
 */
public class Chemistry extends Loggable{
	
	/**
	 * params contains the kinetic parameters of the mechanism, as
	 * well as mininum and maximum constraints on the values,
	 * and information on whether they will be fitted or not
	 */
	private Parameters2D params;
	private LinkedList<String> species;

	
	//number of parameters per reaction (modified Arrhenius [A,n,Ea]): 3
	private static final int NOPARAMETERSPERREACTION = 3;
	
	
	/**
	 * filename of the chemistry input that contains info on elements,
	 * species, TD, TP, mechanism
	 */
	private String chemistryInput;
	
	/**
	 * ############
	 * CONSTRUCTORS
	 * ############
	 */
	public Chemistry(String chemistryInput, Parameters2D params){
		this.chemistryInput = chemistryInput;
		this.params = params;
	}
	
	public Chemistry(){
	}
	
	/**
	 * initial_guess returns the initial parameter guesses, found in the chem.inp file.
	 * It does so by reading the file, searching the key-String "REACTIONS"
	 * from that point on, every line is read and the 2nd and 4th subString is taken and stored in a List l
	 * The 2nd and 4th element correspond to A and Ea of the modified Arrhenius equation
	 * The List l is then converted to a double array and returned
	 * @return initial guesses for kinetic parameters, as double array 
	 * @throws IOException
	 */
	public double[][] initialGuess (String workingDir) throws IOException{
	
		double[][] beta = new double[params.getFixRxns().length][params.getFixRxns()[0].length];
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(workingDir,chemistryInput)));
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
			/**
			 * TODO method of reading kinetics needs to become more robust! 
			 */
			for (int i = 0; i < params.getFixRxns().length; i++){
				dummy = in.readLine();
				String[] st_dummy = dummy.split("\\s");
				for (int j = 0; j < params.getFixRxns()[i].length; j++){
					//start with element at position 1 (not 0), because arrhenius parameters start at position 1!
					beta[i][j] = Double.parseDouble(st_dummy[j+1].trim());
				}
			}
			in.close();
	
		} catch (IOException e) {
			Tools.logger.error("Problem with obtaining initial parameter guesses!");
			System.exit(-1);
		}
		return beta;
	}

	
	/**
	 * ####################
	 * GETTERS AND SETTERS:
	 * ####################
	 */
	/**
	 * @category getter
	 * @return
	 */
	public LinkedList<String> getSpecies() {
		return species;
	}
	
	/**
	 * @category setter
	 * @param species
	 */
	public void setSpecies(LinkedList<String> species) {
		this.species = species;
	}
	/**
	 * @category getter
	 * @return
	 */
	public static int getNoparametersperreaction() {
		return NOPARAMETERSPERREACTION;
	}
	/**
	 * @category getter
	 * @return
	 */
	public String getChemistryInput() {
		return chemistryInput;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setChemistryInput(String chemistryInpput) {
		this.chemistryInput = chemistryInpput;
	}
	/**
	 * @category getter
	 * @return
	 */
	public Parameters2D getParams() {
		return params;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setParams(Parameters2D params) {
		this.params = params;
	}

	/**
	 * getSpeciesNames retrieves the names of the species from the chemistry input file:
	 * *.asu file
	 * @param in TODO
	 * @return
	 * @throws IOException
	 */
	public static List<String> readSpeciesNames(BufferedReader in)throws IOException{
		
		List<String> namesList = new LinkedList<String>();
	
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
}
