package readers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that contains information that specifies the entire reactor:
 * <LI> Reactor Input Name
 * <LI> Reactor Length
 * <LI> Reactor Diameter
 * <LI> Map with species names and their corresponding molecular weights
 * <LI> An axial temperature profile: Map with axial position and corresponding T [K]
 * <LI> An axial pressure profile: Map with axial position and corresponding P [bar abs]
 * <LI> Absolute solver tolerance
 * <LI> Relative solver tolerance
 * 
 * <BR> This can be read from an input file.
 * @author nmvdewie
 *
 */
public class PFRReactorInput extends ReactorInput{
	
	REACTORTYPE type = REACTORTYPE.PFR;
			
	//Reactor Specs:
	public Double length;
	
	public Double diameter;
	
	public Map<Double, Double> t_profile = new HashMap<Double, Double>();;
	
	public Map<Double, Double> p_profile = new HashMap<Double, Double>();;
	
	//Chemistry Specs:
	public int NOS;//Number of Species
	
	public List<String> species_name;
	
	public Map<String,Double> species = new HashMap<String,Double>();;
	
	public List<Double> species_mw;

	public double total_massflrt;
	
	//Solver specs:
	public Double atol = 1.e-9;//chemkin default 
	
	public Double rtol = 1.e-6;//chemkin default;
	
}
