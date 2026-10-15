package AgenteEuclidiano;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * Percepción del solucionador: los INFORM del generador.
 * Cada estado es un int[9] por filas, 0 = espacio vacío; llegan como int[][].
 */
public class Percepcion {

    private final MessageTemplate plantilla = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchSender(new AID(AgenteEuclidiano.GENERADOR, AID.ISLOCALNAME)));

    public ACLMessage percibir(Agent agente) {
        return agente.receive(plantilla);
    }

    /** @return los estados del mensaje, o null si el contenido no es un int[][] */
    public int[][] leerVecinos(ACLMessage msg) {
        try {
            return msg.getContentObject() instanceof int[][] vecinos ? vecinos : null;
        } catch (UnreadableException e) {
            return null;
        }
    }

    /**
     * Un puzzle nuevo llega sin "in-reply-to" (el generador lo manda por su cuenta);
     * los vecinos de un nodo pedido llegan como respuesta a nuestro REQUEST.
     */
    public boolean esNuevoPuzzle(ACLMessage msg) {
        return msg.getInReplyTo() == null;
    }
}
