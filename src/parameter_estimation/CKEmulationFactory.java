package parameter_estimation;

import readers.ReactorSetupInput;
import chemkin_wrappers.AbstractChemkinRoutine;
import chemkin_wrappers.BatchDecorator;
import chemkin_wrappers.CSTRDecorator;
import chemkin_wrappers.LaminarFlameDecorator;
import chemkin_wrappers.PFRDecorator;

public class CKEmulationFactory {

	AbstractChemkinRoutine routine;
	
	public CKEmulationFactory(AbstractChemkinRoutine routine) {
		this.routine = routine;
	}

	public AbstractChemkinRoutine createRoutine(String model) {
		//PFR
		if(model.equals(ReactorSetupInput.MODEL.PFR)){
			return new PFRDecorator(routine);//decoration of parent chemkin routine:
			
		}

		//CSTR
		else if (model.equals(ReactorSetupInput.MODEL.CSTR)){
			return new CSTRDecorator(routine);//decoration of parent chemkin routine:
			
		}

		//ignition delay, batch reactor, transient solver, as in shock tube experiments
		else if (model.equals(ReactorSetupInput.MODEL.IGNITION_DELAY)){
			return new BatchDecorator(routine);//decoration of parent chemkin routine:
			
		}
		//freely propagating laminar flame (flame speed experiments):
		else if(model.equals(ReactorSetupInput.MODEL.FLAME_SPEED)	){
			return new LaminarFlameDecorator(routine);//decoration of parent chemkin routine:
			
		}
		return null;
	}

}
