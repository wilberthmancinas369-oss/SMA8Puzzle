package AgenteGenerador;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * Percepción del Agente Generador: lee del buzón los REQUEST con un nodo.
 * El nodo viaja como int[9] por filas, 0 = espacio vacío.
 */
public class Percepcion {

    private final MessageTemplate plantilla = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

    public ACLMessage percibir(Agent agente) {
        return agente.receive(plantilla);
    }

    /** @return el nodo del mensaje, o null si el contenido no es un int[] */
    public int[] leerNodo(ACLMessage msg) {
        try {
            return msg.getContentObject() instanceof int[] nodo ? nodo : null;
        } catch (UnreadableException e) {
            return null;
        }
    }
}
