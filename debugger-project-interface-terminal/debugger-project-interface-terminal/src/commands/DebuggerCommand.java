package commands;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import dbg.Debugger;

public interface DebuggerCommand {

    /**
     * Exécution d'une commande.
     *
     * @param vm    {@link VirtualMachine} Une machine virtuelle destinée au débogage.
     * @param event {@link Event} Une occurrence dans une VM cible qui présente un intérêt pour un débogueur.
     * @param args  {@link Object} Argument passé à la méthode. Utile lorsque l'on veut par exemple afficher le contenu d'une variable
     *              bien définie.
     * @return Vrai si la commande doit continuer l'exécution de la vm, faux sinon.
     */
    boolean execute(VirtualMachine vm, Event event, Debugger debugger, Object... args) throws Exception;
}

