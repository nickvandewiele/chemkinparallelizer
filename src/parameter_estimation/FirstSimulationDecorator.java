package parameter_estimation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import chemkin_wrappers.AbstractChemkinRoutine;
import chemkin_wrappers.ChemkinRoutine;
import chemkin_wrappers.CreateSolnListDecorator;

/**
 * 
 * Decorator implementation of {@link CKEmulationDecorator}.
 *  Creates and adapts CKSolnList.txt file.
 * @author nmvdewie
 *
 */
public class FirstSimulationDecorator extends CKEmulationDecorator{

	public FirstSimulationDecorator(AbstractCKEmulation sim){
		super.simulation = sim;
	}

	public void run(){

		//first run reactor model
		simulation.start();
		try {
			simulation.join();//wait until 1st simulatio is finished.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//now create CKSolnList:
		//instantiation of parent chemkin routine:
		AbstractChemkinRoutine routine = new ChemkinRoutine(getConfig());
		routine.reactorDir = getReactorDir();
		routine.reactorOut = getReactorOut();
		routine.reactorSetup = getReactorInput().filename;
		routine = new CreateSolnListDecorator(routine);//decoration of parent chemkin routine:
		routine.executeCKRoutine();//execution

		writeSolnList();
		
		//copy from reactor dir (where CKSolnList was first created, to working dir for reuse for other simulations:
		Tools.copyFile(getReactorDir()+ChemkinConstants.CKSOLNLIST,getConfig().paths.getWorkingDir()+ChemkinConstants.CKSOLNLIST);
	
		
		

	}

	/**
	 * Set the SolnList.txt to the desired format:<BR>
	 * <LI>lines with # are left untouched</LI>
	 * <LI>no information whatsoever for all variables, except species, MW, exit_mass_flow_rate</LI>
	 * <LI>no sensitivity info for species, MW, exit_mass_flow_rate</LI>
	 * <LI>set UNIT of distance to (cm)</LI>
	 * <LI>all species mole fractions are reported, also those with negative fractions: FILTER MIN</LI>
	 * @param in TODO
	 * @throws Exception
	 * TODO when dealing with shock tube experiments during which ignition delays are measured, the response variable
	 * is ignition delay and not a species mass fraction.
	 */
	public void writeSolnList(){
		BufferedReader in;
		PrintWriter out;
		String temp = null;
		try {
			in = new BufferedReader(new FileReader(new File(getReactorDir(),ChemkinConstants.CKSOLNLIST)));
			temp = "tempList.txt";
			out = new PrintWriter(new FileWriter(new File(getReactorDir(),temp)));


			File speciesFile = new File(getConfig().paths.getWorkingDir(),ChemkinConstants.CHEMASU);
			BufferedReader inSpecies = new BufferedReader (new FileReader(speciesFile));
			List<String> speciesNames = Chemistry.readSpeciesNames(inSpecies);
			String dummy = null;
			dummy = in.readLine();

			//if a comment line (starts with char '#') is read, just copy it to output file
			while(in.ready()){
				//if a blank line is read, just copy and continue
				if (dummy.trim().length()==0){
					out.println(dummy);
					dummy = in.readLine();
					//System.out.println(dummy);
				}
				//if a comment line (#) is read, just copy and continue
				else if (dummy.charAt(0)=='#'||(dummy.trim().length()==0)) {
					out.println(dummy);
					dummy = in.readLine();
					//System.out.println(dummy);
				}
				else {
					//separator are TWO spaces, not just one space!
					String[] st_dummy = dummy.split("  ");
					//only species variables and molecular weight variable are reported:
					if (st_dummy[0].trim().equals("VARIABLE")){
						//check if the 2nd keyword matches "molecular weight":
						if (st_dummy[1].trim().equals("molecular_weight")){							
							//no sensitivity info for molecular weight variable
							st_dummy[4]="0";
						}
						//check if the 2nd keyword matches "exit_mass_flow_rate":
						else if(st_dummy[1].trim().equals("exit_mass_flow_rate")){
							st_dummy[4]="0";
						}
						//check if 2nd keyword is one of the species names:
						else if(speciesNames.contains(st_dummy[1])){
							//no sensitivity info for species: set last number in the line to zero
							st_dummy[4]="0";
						}
						//ignition data should also considered:
						else if(st_dummy[1].trim().equals("ignition_data")){
							//check whether this experiments is about ignition delays:

							//no sensitivity
							st_dummy[4]="0";


						}
						else if(st_dummy[1].trim().equals("flame_speed")){
							//check whether this experiments is about flame speeds:

							//no sensitivity
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
					else if(st_dummy[0].trim().equals("UNIT")){
						if (st_dummy[1].trim().equals("Distance")){
							st_dummy[2]="(cm)";
						}
					}

					//make sure even negative mole fractions are printed:
					else if(st_dummy[0].trim().equals("FILTER")){
						if (st_dummy[1].trim().equals("MIN")){
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
			in.close();
			out.close();
		}


		catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//remove old CKSolnList and replace it with newly create one
		File old = new File(getReactorDir(),ChemkinConstants.CKSOLNLIST);
		old.delete();
		File f_temp = new File(getReactorDir(),temp);
		f_temp.renameTo(new File(getReactorDir(),ChemkinConstants.CKSOLNLIST));
	}

}
