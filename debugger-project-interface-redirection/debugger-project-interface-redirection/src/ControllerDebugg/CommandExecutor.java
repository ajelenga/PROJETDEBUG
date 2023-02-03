package ControllerDebugg;

import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.StepRequest;
import dbg.Breakpoint;
import dbg.Debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandExecutor {
    private VirtualMachine vm;
    private Event event;

    private Debugger debugger;
    private Object[] args;
    private StepRequest sr;
    private boolean state = false;

    public CommandExecutor(VirtualMachine vm, Event event, Debugger debugger, Object... args) {
        this.vm = vm;
        this.event = event;
        this.debugger = debugger;
        this.args = args;
    }

    StepRequest getSrStep_Over(VirtualMachine vm, Event event) {
        if (event instanceof StepEvent) event.request().disable();
        if (sr == null) {
            sr = vm.eventRequestManager()
                    .createStepRequest(((LocatableEvent) event).thread(), StepRequest.STEP_LINE, StepRequest.STEP_OVER);
        }
        return sr;
    }

    StepRequest getSrStep(VirtualMachine vm, Event event) {
        if (event instanceof StepEvent) event.request().disable();
        if (sr == null) {
            sr = vm.eventRequestManager()
                    .createStepRequest(((LocatableEvent) event).thread(), StepRequest.STEP_LINE, StepRequest.STEP_INTO);
        }
        return sr;
    }

    public VirtualMachine getVm() {
        return vm;
    }

    public void setVm(VirtualMachine vm) {
        this.vm = vm;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public void setDebugger(Debugger debugger) {
        this.debugger = debugger;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    /**
     * Execute la prochaine instruction. S’il s’agit d’un appel de méthode,
     * l’exécution entre dans cette dernière.
     *
     * @return true
     */
    public boolean enableStepRequestStepCommand() {
        sr = getSrStep(vm, event);
        sr.enable();
        return true;
    }

    /**
     * Execute la ligne courante.
     *
     * @return true
     */
    public boolean enableStepRequestStepCommand_Over() {
        sr = getSrStep_Over(vm, event);
        sr.enable();
        return true;
    }

    /**
     * Renvoie et imprime la liste des arguments de la méthode en
     * cours d’exécution, sous la forme d’un couple nom → valeur.
     *
     * @return true
     */
    public boolean enableStepRequestArgumentsCommand() throws Exception {
        StackFrame frame = ((LocatableEvent) event).thread().frame(0);
        List<LocalVariable> localVariables = frame.visibleVariables();

        localVariables.forEach(localVariable -> {
            if (localVariable.isArgument()) {
                Value value = frame.getValue(localVariable);
                System.out.println("-----------------Arguments---------------------");
                System.out.printf("\t %s -> %s \n", localVariable.name(), value);
                System.out.println("-----------------End Arguments-----------------\n");
            }
        });
        debugger.readSystemIn(vm, event);
        return true;
    }

    /**
     * Continue l’exécution jusqu’au prochain point d’arrêt. La granular-
     * ité est l’instruction step.
     *
     * @return true
     */
    public boolean enableStepRequestContinueCommand() throws Exception {
        event.request().disable();
        return true;
    }

    /**
     * Renvoie et imprime la frame courante.
     *
     * @return true
     */
    public boolean enableStepRequestFrameCommand() throws Exception {
        StackFrame frame = ((LocatableEvent) event).thread().frame(0);
        System.out.println("-----------------Frame---------------------\n");
        System.out.println(frame + "\n");
        System.out.println("-----------------End Frame-----------------\n");
        debugger.readSystemIn(vm, event);
        return true;
    }

    /**
     * Renvoie et imprime la méthode en cours d’exécution.
     *
     * @return true
     */
    public boolean enableStepRequestMethodCommand() throws Exception {
        StackFrame frame = ((LocatableEvent) event).thread().frame(0);
        Method method = frame.location().method();
        String arguments = method
                .arguments()
                .stream()
                .map(localVariable -> localVariable.typeName() + " " + localVariable.name())
                .collect(Collectors.joining());
        System.out.println("-----------------Method---------------------");
        System.out.printf("\t %s %s(%s) \n", method.returnTypeName(), method.name(), arguments);
        System.out.println("-----------------End Method-----------------\n");
        debugger.readSystemIn(vm, event);
        return true;
    }

    /**
     * Imprime la valeur de la variable passée en
     * paramètre.
     *
     * @return true
     */
    public boolean enableStepRequestPrintVarCommand(Object... args) throws Exception {
        state = false;
        if (args.length < 1) {
            System.out.println("Command 'print-var' parametre attendu ");
        } else {
            StackFrame frame = ((LocatableEvent) event).thread().frame(0);
            ArrayList tabArg = (ArrayList) args[0];
            if (tabArg.size() == 1) {
                String arg = tabArg.get(0).toString();
                LocalVariable localVariable = frame.visibleVariableByName(arg);
                if (localVariable != null) {
                    Value value = frame.getValue(localVariable);
                    System.out.println("-----------------PrintVar---------------------");
                    System.out.printf("\t %s -> %s \n\n", localVariable.name(), value);
                    System.out.println("-----------------End PrintVar-----------------\n");
                    state = true;
                } else {
                    System.out.printf("la valeur de la variable " + arg + " est null\n");
                }
            } else {
                System.out.println("Command 'print-var' nombre de parametre attendu = 1");
            }
        }
        debugger.readSystemIn(vm, event);
        return state;
    }


    /**
     * Renvoie le receveur de la méthode courante (this).
     *
     * @return true
     */
    public boolean enableStepRequestReceiverCommand() throws Exception {
        state = false;
        StackFrame frame = ((LocatableEvent) event).thread().frame(0);
        System.out.println("-----------------Receiver---------------------");

        if (frame.thisObject() == null) {
            System.out.println("method static " + frame.location().method().name());
            state = false;
        } else {
            System.out.printf("\t %s \n", frame.thisObject());
            state = true;
        }

        System.out.println("-----------------End Receiver-----------------\n");
        debugger.readSystemIn(vm, event);
        return state;
    }

    /**
     * Renvoie et imprime la liste des variables d’instance du
     * receveur courant, sous la forme d’un couple nom → valeur .
     *
     * @return true
     */
    public boolean enableStepRequestReceiverVariablesCommand() throws Exception {
        state = false;
        StackFrame frame = ((LocatableEvent) event).thread().frame(0);
        ObjectReference objectReference = frame.thisObject();
        if (objectReference == null) {
            System.out.println("-----------------Receiver-Var--------------------");
            System.out.println("method static " + frame.location().method().name());
            System.out.println("--------------End Receiver-Var-------------------\n");
            state = false;
        } else {
            System.out.println("-----------------Receiver-Var--------------------");
            Map<Field, Value> fields = objectReference.getValues(objectReference.referenceType().allFields());
            for (Map.Entry<Field, Value> field : fields.entrySet()) {
                System.out.printf("\t %s -> %s \n", field.getKey().name(), field.getValue());
            }
            System.out.println("-----------------End Receiver-Var-----------------\n");
            state = true;
        }
        debugger.readSystemIn(vm, event);
        return state;
    }

    /**
     * Renvoie l’objet qui a appelé la méthode courante.
     *
     * @return true
     */
    public boolean enableStepRequestSenderCommand() throws Exception {
        try {
            StackFrame frameParent = ((LocatableEvent) event).thread().frame(1);
            System.out.printf("\t %s \n", frameParent);
            debugger.readSystemIn(vm, event);
        } catch (IndexOutOfBoundsException ignored) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Renvoie la pile d’appel de méthodes qui a amené l’exécution au point
     * courant.
     *
     * @return true
     */
    public boolean enableStepRequestStackCommand() throws Exception {
        List<StackFrame> frames = ((LocatableEvent) event).thread().frames();
        for (int i = frames.size() - 1; i >= 0; i--) {
            Method method = frames.get(i).location().method();
            System.out.println("-----------------Stack---------------------");
            System.out.printf("\t %s: arguments(%s), return(%s) \n", method.name(), method.arguments().toString(), method.returnTypeName());
            System.out.println("-----------------END Stack-----------------\n");
            debugger.readSystemIn(vm, event);
        }
        return true;
    }

    /**
     * Renvoie et imprime la liste des variables temporaires de la frame
     * courante, sous la forme de couples nom → valeur.
     *
     * @return true
     */
    public boolean enableStepRequestTemporiesCommand() throws Exception {
        StackFrame frame = ((LocatableEvent) event).thread().frame(0);
        List<LocalVariable> localVariables = frame.visibleVariables();

        localVariables.forEach(localVariable -> {
            Value value = frame.getValue(localVariable);
            System.out.println("-----------------Temporaries---------------------");
            System.out.printf("\t %s -> %s \n", localVariable.name(), value);
            System.out.println("--------------END Temporaries--------------------\n");
        });
        debugger.readSystemIn(vm, event);
        return false;
    }

    public boolean enableStepRequestBreakCommand(Object... args) throws Exception {
        state = false;
        if (args.length < 1) {
            System.err.println("Command 'break' 2 parametre attendu ");
        } else {
            ArrayList tabArg = (ArrayList) args[0];
            if (tabArg.size() == 2) {
                String filename = tabArg.get(0).toString();
                int lineNumber = 0;
                try {
                    lineNumber = Integer.parseInt(tabArg.get(1).toString());
                } catch (NumberFormatException r) {
                    System.err.println("parametre d'entier attendu au lieu de: " + tabArg.get(1).toString());
                }
                Breakpoint bp = new Breakpoint();
                bp.setClassName("dbg." + filename);
                bp.setLineNumber(lineNumber);
                debugger.setBreakPoint(vm, bp);
                if (bp.getBreakpointRequest() != null) {
                    System.out.println("-----------------Break point créé à la ligne" + lineNumber + " ---------------------\n");
                    state = true;
                } else {
                    System.err.println("break ko");
                    state = false;
                }

            } else {
                System.out.println("Command 'break' nombre de parametre attendu = 2");
            }
        }
        debugger.readSystemIn(vm, event);
        return state;
    }

    public boolean enableStepRequestBreakpointsCommand() throws Exception {
        state = false;
        System.out.println("------------------Breakpoints----------------------");
        for (BreakpointRequest breakpointRequest : vm.eventRequestManager().breakpointRequests()) {
            if (breakpointRequest.isEnabled()) {
                System.out.println("\t Class: " + breakpointRequest.location().sourceName() + " ligne: " + breakpointRequest.location().lineNumber());
                state = true;
            }
        }
        System.out.println("------------------End Breakpoints-------------------\n");
        debugger.readSystemIn(vm, event);
        return state;
    }

    public boolean enableStepRequestBreakOnceCommand(Object... args) throws Exception {
        state = false;
        if (args.length < 1) {
            System.err.println("Command 'break-once' 2 parametre attendu ");
        } else {
            ArrayList tabArg = (ArrayList) args[0];
            if (tabArg.size() == 2) {
                String filename = tabArg.get(0).toString();
                int lineNumber = 0;
                try {
                    lineNumber = Integer.parseInt(tabArg.get(1).toString());
                } catch (NumberFormatException r) {
                    System.err.println("parametre d'entier attendu au lieu de: " + tabArg.get(1).toString());
                }
                Breakpoint bp = new Breakpoint();
                bp.setClassName("dbg." + filename);
                bp.setLineNumber(lineNumber);
                debugger.setBreakPointOnce(vm, bp);
                if (bp.getBreakpointRequest() != null) {
                    System.out.println("-----------------break-once point créé " + lineNumber + " ---------------------\n");

                    state = true;
                } else {
                    System.err.println("break-once ko");
                    state = false;
                }

            } else {
                System.err.println("Command 'break-once' nombre de parametre attendu = 2");
            }
        }
        debugger.readSystemIn(vm, event);
        return state;
    }

    public boolean enableStepRequestBreakOnCountCommand(Object... args) throws Exception {
        state = false;
        if (args.length < 1) {
            System.out.println("Command 'break-on-count' 2 parametre attendu ");
        } else {
            ArrayList tabArg = (ArrayList) args[0];
            if (tabArg.size() == 3) {
                String filename = tabArg.get(0).toString();
                int lineNumber = 0;
                int count = 0;
                try {
                    lineNumber = Integer.parseInt(tabArg.get(1).toString());
                    count = Integer.parseInt(tabArg.get(2).toString());

                } catch (NumberFormatException r) {
                    System.err.println("parametre d'entier attendu au lieu de: " + tabArg.get(1).toString());
                }
                Breakpoint bp = new Breakpoint();
                bp.setClassName("dbg." + filename);
                bp.setLineNumber(lineNumber);
                bp.setCount(count);
                debugger.setBreakPointCount(vm, bp);
                if (bp.getBreakpointRequest() != null) {
                    System.out.println("val = " + bp.getCount());
                    System.out.println("-----------------break-on-count point créé mais pas actif " + lineNumber + " ---------------------\n");
                    state = true;
                } else {
                    System.err.println("break-on-count ko");
                    state = false;
                }

            } else {
                System.err.println("Command 'break-on-count' nombre de parametre attendu = 3");
            }
        }
        debugger.readSystemIn(vm, event);
        return state;
    }

    public boolean enableStepRequestBreakBeforeMethodCallCommand(Object... args) {
        return state;
    }

    public boolean enableStepRequestStopCommand() {
        vm.exit(0);
        //System.exit(0);
        return true;
    }
}
