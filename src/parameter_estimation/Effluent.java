package parameter_estimation;

import java.util.Map;
/**
 * type that unites effluent information of a reactor type, i.e.
 * effluent composition, species names
 * @author nmvdewie
 *
 */
public class Effluent {
	private Map<String,Double> speciesFractions;
	/**
	 * @category getter
	 * @return
	 */
	public Map<String, Double> getSpeciesFractions() {
		return speciesFractions;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setSpeciesFractions(Map<String, Double> speciesFractions) {
		this.speciesFractions = speciesFractions;
	}
	public Effluent(){

	}
}

