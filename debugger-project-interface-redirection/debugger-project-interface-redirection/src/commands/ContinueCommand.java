package commands;

import ControllerDebugg.CommandExecutor;
import annotations.Command;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import dbg.Debugger;

@Command(commandName = "continue")
public class ContinueCommand implements DebuggerCommand {

    @Override
    public boolean execute(VirtualMachine vm, Event event, Debugger debugger, Object... args) throws Exception {
        CommandExecutor commandExecutor = new CommandExecutor(vm, event, debugger, args);
        return commandExecutor.enableStepRequestContinueCommand();
    }
}

