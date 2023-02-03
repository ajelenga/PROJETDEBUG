package jfx.sample.controller;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.StepRequest;
import commands.*;
import dbg.Breakpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;

public class SDTest {
    /**
     * représente la classe avec le main du programme à debugger
     */
    private Class debugClass;
    /**
     * représente la machine virtuelle sur laquelle s’exécute la debugClass
     */
    private VirtualMachine vm;
    private Map<String, DebuggerCommand> debuggerCommandMap;

    private List<Breakpoint> mapBreakpointl = new ArrayList();

    private HashMap<Breakpoint, Integer> mapBreakpointWithCount = new HashMap<>();

    public SDTest() {
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

    /**
     * Scriptable Debugger
     */

    public VirtualMachine connectAndLaunchVM() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(debugClass.getName());
        VirtualMachine vm = launchingConnector.launch(arguments);
        return vm;
    }

    private void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter((debugClass.getName()));
        classPrepareRequest.enable();
    }


    public void attachTo(Class debuggeeClass) {
        new SDTest();

        this.debugClass = debuggeeClass;
        try {
            vm = connectAndLaunchVM();
            // Interception de la préparation de la debugClass en mémoire
            // afin d’être certain qu’elle soit connue de la VM.
            enableClassPrepareRequest(vm);
            startDebugger();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        } catch (VMStartException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        } catch (VMDisconnectedException e) {
            System.out.println("Virtual Machine is disconnected: " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void breakpointReached(Event event) throws AbsentInformationException {
        for (Breakpoint bp : this.getMapBreakpointl()) {
            if (((LocatableEvent) event).location().lineNumber() == bp.getLineNumber()) {
                bp.disableBreakpointRequest();
                System.out.println("breakpoints disable");
            }
        }
    }

    public void breakpointReachedOnCount(Event event) throws AbsentInformationException {
        for (Map.Entry<Breakpoint, Integer> entry : this.getMapBreakpointWithCount().entrySet()) {
            Breakpoint key = entry.getKey();
            Integer value = entry.getValue();
            if (((LocatableEvent) event).location().lineNumber() == key.getLineNumber()) {
                if (key.getLineNumber() == value) {
                    key.enableBreakpointRequest();
                } else {
                    entry.setValue(value + 1);
                }
            }
        }
    }

    public void readSystemIn(VirtualMachine virtualMachine, Event e) throws Exception {
        System.out.println("Line: " + ((LocatableEvent) e).location().lineNumber() + " =====> Event: " + e);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Command: ");
        String systemIn = br.readLine();
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
                //this.getCommand(command).execute(vm, e, this, ListArgs);
            } else {
                //this.getCommand(command).execute(virtualMachine, e, this);
            }
        } catch (RuntimeException r) {
            System.err.println("La commande \"" + command + "\" n'est pas prise en charge ");
            this.readSystemIn(virtualMachine, e);
        }
    }

    public void startDebugger() throws Exception {

        EventSet eventSet = null;
        while ((eventSet = vm.eventQueue().remove()) != null) {
            for (Event event : eventSet) {
                //System.out.println(event.toString());
                // L’exécution de notre debuggee est terminée,
                // Sortir de notre boucle de gestion d’évènements.
                if (event instanceof VMDisconnectEvent) {
                    System.out.println("=== End of program ===");
                    return;
                }
                if (event instanceof ClassPrepareEvent) {
                    System.out.println(debugClass.getName());
                    Breakpoint bp = new Breakpoint();
                    bp.setClassName(debugClass.getName());
                    bp.setLineNumber(6);
                    this.setBreakPoint(vm, bp);
                    //debugger.setBreakPoint(vm,debugClass.getName(),8);
                }

                if (event instanceof BreakpointEvent) {
                    for (StepRequest stp : vm.eventRequestManager().stepRequests()) {
                        stp.disable();
                    }
                    this.readSystemIn(vm, event);
                }
                if (event instanceof StepEvent) {
                    //numero de la line à laquelle est le step actuel
                    //System.out.println("package: " +((StepEvent)event).location().declaringType().signature().split("/")[0]);
                    if (((StepEvent) event).location().declaringType().signature().split("/")[0].equals("Ldbg")) {
                        breakpointReached(event);
                        breakpointReachedOnCount(event);
                        this.readSystemIn(vm, event);
                    }
                }

                // Récupération les informations imprimées sur le flux de la VM
                System.out.println("=== Debuggee output ====");
                InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
                OutputStreamWriter writer = new OutputStreamWriter(System.out);
                char[] buf = new char[vm.process().getInputStream().available()];
                reader.read(buf);
                writer.write(buf);
                writer.flush();
                vm.resume();
            }
        }
    }
}
