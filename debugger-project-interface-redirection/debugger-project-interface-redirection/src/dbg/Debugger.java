package dbg;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import commands.*;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class Debugger {
    private Map<String, DebuggerCommand> debuggerCommandMap;

    private List<Breakpoint> mapBreakpointl = new ArrayList();

    private HashMap<Breakpoint, Integer> mapBreakpointWithCount = new HashMap<>();

    public Debugger() {
        this.debuggerCommandMap = new HashMap<>();
        //itinialisation des command
        StepCommand stepCommand = new StepCommand();
        ContinueCommand continueCommand = new ContinueCommand();
        StepOverCommand stepOverCommand = new StepOverCommand();
        StackCommand stackCommand = new StackCommand();
        FrameCommand frameCommand = new FrameCommand();
        ArgumentsCommand argumentsCommand = new ArgumentsCommand();
        MethodCommand methodCommand = new MethodCommand();
        PrintVarCommand printVarCommand = new PrintVarCommand();
        ReceiverCommand receiverCommand = new ReceiverCommand();
        ReceiverVariablesCommand receiverVariablesCommand = new ReceiverVariablesCommand();
        SenderCommand senderCommand = new SenderCommand();
        TemporariesCommand temporariesCommand = new TemporariesCommand();
        BreakCommand breakCommand = new BreakCommand();
        BreakpointsCommand breakpointsCommand = new BreakpointsCommand();
        BreakOnceCommand breakOnceCommand = new BreakOnceCommand();
        BreakOnCountCommand breakOnCountCommand = new BreakOnCountCommand();
        BreakBeforeMethodCallCommand breakBeforeMethodCallCommand = new BreakBeforeMethodCallCommand();
        StopCommand stopCommand = new StopCommand();

        //ajouts dans la map
        this.debuggerCommandMap.put("step", stepCommand);
        this.debuggerCommandMap.put("continue", continueCommand);
        this.debuggerCommandMap.put("step-over", stepOverCommand);
        this.debuggerCommandMap.put("stack", stackCommand);
        this.debuggerCommandMap.put("frame", frameCommand);
        this.debuggerCommandMap.put("arguments", argumentsCommand);
        this.debuggerCommandMap.put("method", methodCommand);
        this.debuggerCommandMap.put("print-var", printVarCommand);
        this.debuggerCommandMap.put("receiver", receiverCommand);
        this.debuggerCommandMap.put("receiver-variables", receiverVariablesCommand);
        this.debuggerCommandMap.put("sender", senderCommand);
        this.debuggerCommandMap.put("temporaries", temporariesCommand);
        this.debuggerCommandMap.put("break", breakCommand);
        this.debuggerCommandMap.put("breakpoints", breakpointsCommand);
        this.debuggerCommandMap.put("break-once", breakOnceCommand);
        this.debuggerCommandMap.put("break-on-count", breakOnCountCommand);
        this.debuggerCommandMap.put("break-before-m-c-c", breakBeforeMethodCallCommand);
        this.debuggerCommandMap.put("stop", stopCommand);
    }

    public HashMap<Breakpoint, Integer> getMapBreakpointWithCount() {
        return mapBreakpointWithCount;
    }

    public List<Breakpoint> getMapBreakpointl() {
        return mapBreakpointl;
    }

    public Map<String, DebuggerCommand> getDebuggerCommandMap() {
        return debuggerCommandMap;
    }

    public DebuggerCommand getCommand(String command) throws Exception {
        if (!debuggerCommandMap.containsKey(command)) {
            throw new RuntimeException(String.format("La commande \"%s\" n'est pas prise en charge", command));
        } else {
            return debuggerCommandMap.get(command);
        }
    }

    public void readSystemIn(VirtualMachine virtualMachine, Event e) throws Exception {

        System.out.println("Line: " + ((LocatableEvent) e).location().lineNumber() + " =====> Event: " + e);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Command: ");
        //String systemIn = br.readLine();
        String systemIn = "step";
        //System.out.println(systemIn);
        String[] tabCommandAgr = systemIn.split(" ");
        //command
        String command = tabCommandAgr[0];

        //tableau d'arguments
        List<String> ListArgs = new ArrayList<>();
        if (tabCommandAgr.length > 1) {
            ListArgs.addAll(Arrays.asList(tabCommandAgr).subList(1, tabCommandAgr.length));
        }
        try {
            if (!ListArgs.isEmpty()) {
                this.getCommand(command).execute(virtualMachine, e, this, ListArgs);
            } else {
                this.getCommand(command).execute(virtualMachine, e, this);
            }
        } catch (RuntimeException r) {
            System.err.println("La commande \"" + command + "\" n'est pas prise en charge ");
            this.readSystemIn(virtualMachine, e);
        }
    }

    public void readSystemIn2(VirtualMachine virtualMachine, Event e, TextField textField) throws Exception {
        System.out.println("Line: " + ((LocatableEvent) e).location().lineNumber() + " =====> Event: " + e);
        System.out.println("Command: ");
        String systemIn = textField.getText();
        String[] tabCommandAgr = systemIn.split(" ");
        //command
        String command = tabCommandAgr[0];

        //tableau d'arguments
        List<String> ListArgs = new ArrayList<>();
        if (tabCommandAgr.length > 1) {
            ListArgs.addAll(Arrays.asList(tabCommandAgr).subList(1, tabCommandAgr.length));
        }
        try {
            if (!ListArgs.isEmpty()) {
                this.getCommand(command).execute(virtualMachine, e, this, ListArgs);
            } else {
                this.getCommand(command).execute(virtualMachine, e, this);
            }
        } catch (RuntimeException r) {
            System.err.println("La commande \"" + command + "\" n'est pas prise en charge ");
            this.readSystemIn2(virtualMachine, e, textField);
        }
    }


    public void setBreakPoint(VirtualMachine virtualMachine, Breakpoint breakpoint) throws AbsentInformationException {
        for (ReferenceType targetClass : virtualMachine.allClasses())
            if (targetClass.name().equals(breakpoint.getClassName())) {
                Location location = targetClass.locationsOfLine(breakpoint.getLineNumber()).get(0);
                breakpoint.setBreakpointRequest(virtualMachine.eventRequestManager().createBreakpointRequest(location));
                breakpoint.enableBreakpointRequest();
            }
    }

    public void setBreakPointOnce(VirtualMachine virtualMachine, Breakpoint breakpoint) throws AbsentInformationException {
        for (ReferenceType targetClass : virtualMachine.allClasses())
            if (targetClass.name().equals(breakpoint.getClassName())) {
                Location location = targetClass.locationsOfLine(breakpoint.getLineNumber()).get(0);
                breakpoint.setBreakpointRequest(virtualMachine.eventRequestManager().createBreakpointRequest(location));
                breakpoint.enableBreakpointRequest();
                this.getMapBreakpointl().add(breakpoint);
            }
    }

    public void setBreakPointCount(VirtualMachine virtualMachine, Breakpoint breakpoint) throws AbsentInformationException {
        for (ReferenceType targetClass : virtualMachine.allClasses())
            if (targetClass.name().equals(breakpoint.getClassName())) {
                Location location = targetClass.locationsOfLine(breakpoint.getLineNumber()).get(0);
                breakpoint.setBreakpointRequest(virtualMachine.eventRequestManager().createBreakpointRequest(location));
                breakpoint.disableBreakpointRequest();
                this.mapBreakpointWithCount.put(breakpoint, 0);

            }
    }
}
