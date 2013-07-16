package parsers;

import java.util.ArrayList;
import java.util.List;

import optimization.Fitting;
import optimization.Parameters2D;

import parameter_estimation.Chemistry;
import parameter_estimation.Experiments;
import readers.ReactorInput;
import readers.ReactorSetupInput;
import util.Licenses;
import util.Paths;

/**
 * Class that contains all the input parameters.<p>
 * 
 * The class basically contains pointers to where specific pieces of information are located.
 * E.g. the working directory, the directory of the chemkin executables, the number of chemkin licenses
 * used, etc...
 * 
 * It also provides a toString() method that prints all of the information to a readable format.
 * 
 * All the input parameters will be read in from a XML file
 * 
 * @author nmvdewie
 *
 */
public class ConfigurationInput {
	
	public Paths paths;
	
	public Chemistry chemistry;
	
	public Experiments experiments;
	
	public Licenses licenses;
	
	public ReactorSetupInput [] reactor_setup;
	
	public List<ReactorInput> reactor_inputs;
	
	public Fitting fitting;
	
	public Integer MODE;

	public ConfigurationInput(){
		chemistry = new Chemistry();
		experiments = new Experiments();
		paths = new Paths();
		fitting = new Fitting();

	}

	
	public Integer getMODE() {
		return MODE;
	}

	public void setMODE(Integer mODE) {
		MODE = mODE;
	}

	@Override
	public String toString() {
		for(int i = 0; i < experiments.exp_db.length; i++){
		}
		return "Configuration Input [" +
				"\nWorking Directory =" + paths.getWorkingDir() + 
				",\nChemkin Executable Directory = " + paths.getChemkinDir() + 
				",\nNumber of Licenses = " + licenses.getValue() + 
				",\nChemistry Input = " + chemistry.getChemistryInput() +
				",\nMode = " + MODE + 
				"\nExperiments Database: " + experiments.exp_db + 
				"\nReactor Setups : " + reactor_setup
				+ "]";
	}

	
	/**
	 * Method that converts the read-in info on optimized parameters to a Parameters2D type
	 */
	public void setParameters() {
		
		int noFittedReactions = fitting.optimizedReactions.size();
		
		int [][] fixRxns = new int [noFittedReactions][chemistry.getNoparametersperreaction()];
		for (int i = 0; i < noFittedReactions; i++){
			fixRxns[i][0] = fitting.optimizedReactions.get(i).A;
			fixRxns[i][1] = fitting.optimizedReactions.get(i).N;
			fixRxns[i][2] = fitting.optimizedReactions.get(i).Ea;
			
		}
		
		Parameters2D params = new Parameters2D(fixRxns);
		
		
		double [][] betaMin = new double [noFittedReactions][Chemistry.getNoparametersperreaction()];
		double [][] betaMax = new double [noFittedReactions][Chemistry.getNoparametersperreaction()];
		for (int i = 0; i < noFittedReactions; i++) {
			for (int j = 0; j < Chemistry.getNoparametersperreaction(); j++){
				betaMin[i][j]=0;
				betaMax[i][j]=1e20;
			}
		}

		params.setBetaMin(betaMin);
		params.setBetaMax(betaMax);


		chemistry.setParams(params);
	}
	public void addReactorInput(ReactorSetupInput input) {
		if (reactor_inputs == null){
			reactor_inputs = new ArrayList<ReactorInput>();
		}
		if(input.type.equals(ReactorSetupInput.AUTO)){
			ReactorInputParsable parser;
			parser = new PFRReactorInputParser1(paths.getWorkingDir(),input.location);
			List<ReactorInput> list = parser.parse();
			reactor_inputs.addAll(list);

		}
		else if(input.type.equals(ReactorSetupInput.MANUAL)){
			reactor_inputs.add(new ReactorInput(input.model, input.location));
		}
		
	}

}
