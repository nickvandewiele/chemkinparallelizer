package parameter_estimation;

public class Fitting {
	private Integer maxNoEvaluations;
	private Boolean flagRosenbrock;
	public Boolean getFlagRosenbrock() {
		return flagRosenbrock;
	}

	public void setFlagRosenbrock(Boolean flagRosenbrock) {
		this.flagRosenbrock = flagRosenbrock;
	}

	public Boolean getFlagLM() {
		return flagLM;
	}

	public void setFlagLM(Boolean flagLM) {
		this.flagLM = flagLM;
	}
	private Boolean flagLM;

	public Integer getMaxNoEvaluations() {
		return maxNoEvaluations;
	}

	public void setMaxNoEvaluations(Integer maxNoEvaluations) {
		this.maxNoEvaluations = maxNoEvaluations;
	}
	public Fitting(Integer maxeval){
		this.maxNoEvaluations = maxeval;
	}
	public Fitting(){
		
	}
	
}
