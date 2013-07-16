package optimization;

import java.util.List;

public class Fitting {
	
	public static final String ROSENBROCK = "ROSENBROCK";
	public static final String LEVENBERG = "LEVENBERG";
	
	public enum METHOD {ROSENBROCK, LEVENBERG}
	
	public String method;
	
	private Integer maxNoRosenbrockEvaluations;
	
	public Integer getMaxNoRosenbrockEvaluations() {
		return maxNoRosenbrockEvaluations;
	}

	public void setMaxNoRosenbrockEvaluations(Integer maxNoEvaluations) {
		this.maxNoRosenbrockEvaluations = maxNoEvaluations;
	}
	public Fitting(){
	
	}
	
	public List<OptimizedReaction> optimizedReactions;
	
}
