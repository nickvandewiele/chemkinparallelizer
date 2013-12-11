package chemkin_model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.io.FileUtils;


import readers.ReactorInput;
import util.ChemkinConstants;
import util.Paths;
import util.Semaphore;
import chemkin_wrappers.AbstractChemkinRoutine;
import chemkin_wrappers.ChemkinRoutine;
import chemkin_wrappers.CreateSolnListDecorator;

/**
 * Decorator implementation of {@link CKEmulationDecorator}.
 * @author Nick
 *
 */
public class RegularSimulationDecorator extends CKEmulationDecorator {

	//Semaphore that controls chemkin license check-in and check-outs:
	Semaphore semaphore;

	public RegularSimulationDecorator(ReactorInput reactorSetupInput,
			AbstractCKEmulation abstractCKEmulation, Semaphore semaphore2) {
		
		super.reactorInput = reactorSetupInput;
		super.simulation = abstractCKEmulation;
		this.semaphore = semaphore2;
	}

	@Override
	public void run() {

		semaphore.acquire();
		logger.info("license acquired!"+getReactorInput().filename);	
		try {
			//copy chemistry input to the reactorDir:
			FileUtils.copyFile(new File(Paths.getWorkingDir(),Paths.chemistryInput), new File(getReactorDir(),Paths.chemistryInput));
			//chemkindataDTD:
			FileUtils.copyFile(new File(Paths.getWorkingDir(),ChemkinConstants.CHEMKINDATADTD),new File(getReactorDir(),ChemkinConstants.CHEMKINDATADTD));
		} catch (IOException e) {}

		//Input Folder with user-defined ROP:
		for(File filename: new File(Paths.UDROPDir).listFiles()){//copy all files in this folder to reactor dir
			new File(getReactorDir(), filename.getName());
		}

		//instantiation of parent chemkin routine:
		AbstractChemkinRoutine routine = new ChemkinRoutine();
		routine.reactorDir = getReactorDir();
		routine.reactorOut = getReactorOut();
		routine.reactorSetup = getReactorInput().filename;
		CKEmulationFactory factory = new CKEmulationFactory(routine);
		routine = factory.createRoutine(getReactorInput().type);
		//now create CKSolnList:
		routine = new CreateSolnListDecorator(routine);//decoration of parent chemkin routine:
		routine.executeCKRoutine();//execution
		try {
			//copy reactor diagnostics file to workingdir:
			FileUtils.copyFile(new File(getReactorDir(),getReactorOut()), new File(Paths.getWorkingDir(),getReactorOut()));
		} catch (IOException e) {}

		//release the semaphore:
		semaphore.release();
		logger.info("license released!"+getReactorInput().filename);
				
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


			File speciesFile = new File(Paths.getWorkingDir(),ChemkinConstants.CHEMASU);
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
					//set UNIT of Distance to m instead of cm:
					if(st_dummy[0].trim().equals("UNIT")){
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
