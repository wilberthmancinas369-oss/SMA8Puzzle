package AgenteGenerador;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * Percepción del Agente Generador: lee del buzón los REQUEST con un nodo
 * y los QUERY_REF de la GUI que preguntan cuántos mensajes hubo.
 * El nodo viaja como int[9] por filas, 0 = espacio vacío.
 */
public class Percepcion {

    private final MessageTemplate plantilla = MessageTemplate.or(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF));

    public ACLMessage percibir(Agent agente) {
        return agente.receive(plantilla);
    }

    /** ¿La GUI pregunta cuántos mensajes FIPA intercambió cada solucionador? */
    public boolean esConsultaMensajes(ACLMessage msg) {
        return msg.getPerformative() == ACLMessage.QUERY_REF;
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
