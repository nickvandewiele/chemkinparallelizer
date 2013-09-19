package commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import parsers.ConfigurationInput;
import util.Paths;


public class UpdateChemistryCommand implements Command {
	static Logger logger = Logger.getLogger(UpdateChemistryCommand.class);
	ConfigurationInput config;
	
	double [] parameter_guesses;
	
	public UpdateChemistryCommand(ConfigurationInput config, double [] parameter_guesses){
		this.config = config;
		this.parameter_guesses = parameter_guesses;
		
	}
	public void execute() {
		updateChemistryInput();

	}
	/**	
	 * plug new parameter guesses into the chemkin config.chemistry input file (usually chem.inp)<BR>
	 * write new update chem.inp file <BR>
	 * return the config.chemistry input filename<BR>
	 * WARNING: method supposes TD inside config.chemistry input file!!!<BR>
	 */
	public void updateChemistryInput () {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(new File(Paths.getWorkingDir(),Paths.chemistryInput)));
			PrintWriter out = new PrintWriter(new FileWriter(new File(Paths.getWorkingDir(),"temp.inp")));
			String dummy = in.readLine();
			
			//just copy part of chem.inp about Elements, Species, Thermo
			boolean b = true;
			while(b){
				out.println(dummy);
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
			out.println(dummy);
			
			int counter = 0;
			for (int i = 0; i < ConfigurationInput.chemistry.getParams().getFixRxns().length; i++){
				dummy = in.readLine();
				String[] st_dummy = dummy.split("\\s");
				for (int j = 0; j < ConfigurationInput.chemistry.getParams().getFixRxns()[0].length; j++){
					//put new values of kinetic parameters (at position 1, 2, 3 of st_dummy[]):
					st_dummy[j+1] = Double.toString(parameter_guesses[counter]);
					counter++;
				}
				
				dummy = st_dummy[0];
				for (int j = 1; j < st_dummy.length; j++) {
					dummy = dummy +" "+st_dummy[j];
				}
				logger.info(dummy);
				out.println(dummy);
				
			}
			
			//just copy other reactions that are not varied, until end of file:
			dummy = in.readLine();
			while(!dummy.equals("END")){
				out.println(dummy);
				dummy = in.readLine();
			}
			
			out.println(dummy);
			
			in.close();
			out.close();
			
			File f_old = new File(Paths.getWorkingDir(),Paths.chemistryInput);
			f_old.delete();
			File f = new File(Paths.getWorkingDir(),"temp.inp");
			f.renameTo(new File(Paths.getWorkingDir(),Paths.chemistryInput));
			
			//check if initial input file is error-free:
			Command checkChemistry = new CheckChemistryFileCommand();
			checkChemistry.execute();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
}
