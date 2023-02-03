package dbg;

/**
 * Cette classe contient une méthode main qui lance un debugger sur une classe.
 * <p>
 * debuggerInstance Cet objet implémente le comporte- ment de notre debugger.
 */
public class JDISimpleDebugger {
    ScriptableDebugger debuggerInstance = new ScriptableDebugger();

    public JDISimpleDebugger(Class debuggeeClass) {
        debuggerInstance.attachTo(debuggeeClass);
    }
}

