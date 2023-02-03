package dbg;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.StepRequest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

public class ScriptableDebugger {
    /**
     * représente la classe avec le main du programme à debugger
     */
    private Class debugClass;
    /**
     * représente la machine virtuelle sur laquelle s’exécute la debugClass
     */
    private VirtualMachine vm;
    private Debugger debugger;

    /**
     * Instancie une interface vers une machine virtuelle que nous pourrons manipuler.
     *
     * @return VirtualMachine
     */
    public VirtualMachine connectAndLaunchVM() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(debugClass.getName());
        VirtualMachine vm = launchingConnector.launch(arguments);
        return vm;
    }

    /**
     * Renseigne sa debugClass, instancie une machine virtuelle (connectAndLaunchVM()) puis démarre la session de debugging (startDebugger()).
     *
     * @param debuggeeClass {@link Class} la classe à debugger.
     */
    public void attachTo(Class debuggeeClass) {
        debugger = new Debugger();

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

    /**
     * configure une requête envoyée à la VM.
     * Pour chaque requête active, la VM génère l’évènement correspondant lors de l’exécution du debuggee.
     * Chaque évènement expose via une API une partie du contexte d’exécution du programme que nous pouvons utiliser pour obtenir de l’information et debugger.
     *
     * @param vm {@link VirtualMachine} VirtualMachine
     */
    private void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter((debugClass.getName()));
        classPrepareRequest.enable();
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
                    debugger.setBreakPoint(vm, bp);
                    //debugger.setBreakPoint(vm,debugClass.getName(),8);
                }

                if (event instanceof BreakpointEvent) {
                    for (StepRequest stp : vm.eventRequestManager().stepRequests()) {
                        stp.disable();
                    }
                    debugger.readSystemIn(vm, event);
                }
                if (event instanceof StepEvent) {
                    //numero de la line à laquelle est le step actuel
                    //System.out.println("package: " +((StepEvent)event).location().declaringType().signature().split("/")[0]);
                    if (((StepEvent) event).location().declaringType().signature().split("/")[0].equals("Ldbg")) {
                        breakpointReached(event);
                        breakpointReachedOnCount(event);
                        debugger.readSystemIn(vm, event);
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

    public void breakpointReached(Event event) throws AbsentInformationException {
        for (Breakpoint bp : debugger.getMapBreakpointl()) {
            if (((LocatableEvent) event).location().lineNumber() == bp.getLineNumber()) {
                bp.disableBreakpointRequest();
                System.out.println("breakpoints disable");
            }
        }
    }

    public void breakpointReachedOnCount(Event event) throws AbsentInformationException {
        for (Map.Entry<Breakpoint, Integer> entry : debugger.getMapBreakpointWithCount().entrySet()) {
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

}
