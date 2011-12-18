package parameter_estimation;

/**
 * The ParamEstimationInvoker manages a set of Command objects. 
 * When a specific command is called, via an integer called "mode",
 * it invokes the execute() method on the command. That is the full
 * extent of the Invoker's knowledge of the classes it is invoking
 * as the Commmand object decouples the invoker from the Param_Est
 * class that is doing the actual home-automation work.
 * @author nmvdewie
 *
 */
public class ParamEstimationInvoker {
	Command[] commands;
	
	public ParamEstimationInvoker(){
		int no_commands = 5;//number of possible commands arbitrarily set to 5
		commands = new Command[no_commands];
		Command noCommand = new NoCommand();
		for(Command command: commands){
			command = noCommand;
		}
	}

	public void setCommand(int slot, Command command){
		commands[slot] = command;
	}
	public void performMode(int mode) {
		commands[mode].execute();
	}
}
