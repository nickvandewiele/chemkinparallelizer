package parsers;

import java.util.ArrayList;
import java.util.List;

import chemkin_model.Chemistry;
import datamodel.Experiments;

import optimization.Fitting;
import optimization.Parameters2D;

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
	
	public static Chemistry chemistry = new Chemistry();
	
	public static Experiments experiments = new Experiments();
	
	public static Licenses licenses;
	
	public static ReactorSetupInput [] reactor_setup;
	
	public static List<ReactorInput> reactor_inputs;
	
	public static Fitting fitting = new Fitting();
	
	public static Integer MODE;

	public static Integer getMODE() {
		return MODE;
	}

	public static void setMODE(Integer mODE) {
		MODE = mODE;
	}

	/**
	 * Method that converts the read-in info on optimized parameters to a Parameters2D type
	 */
	public static void setParameters() {
		
		int noFittedReactions = fitting.optimizedReactions.size();
		
		int [][] fixRxns = new int [noFittedReactions][Chemistry.getNoparametersperreaction()];
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
			parser = new PFRReactorInputParser1(input.location);
			List<ReactorInput> list = parser.parse();
			reactor_inputs.addAll(list);

		}
		else if(input.type.equals(ReactorSetupInput.MANUAL)){
			reactor_inputs.add(new ReactorInput(input.model, input.location));
		}
		
	}

}
