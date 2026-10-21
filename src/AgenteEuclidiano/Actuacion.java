package AgenteEuclidiano;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Actuación del solucionador: pedir vecinos al generador e informar el resultado a las GUIs.
 */
public class Actuacion {

    private final Agent agente;
    private final AID generador = new AID(AgenteEuclidiano.GENERADOR, AID.ISLOCALNAME);
    private final AID gui = new AID(AgenteEuclidiano.GUI, AID.ISLOCALNAME);
    private final AID guiSolucionador = new AID(AgenteEuclidiano.GUI_SOLUCIONADOR, AID.ISLOCALNAME);
    private int pedidos = 0; // identifica cada REQUEST para reconocer su respuesta

    public Actuacion(Agent agente) {
        this.agente = agente;
    }

    /** REQUEST (nodo elegido) -> generador. Contenido: int[9]. */
    public void pedirVecinos(int[] estado) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(generador);
        msg.setReplyWith(idPedido(++pedidos)); // la respuesta llega con este in-reply-to
        try {
            msg.setContentObject(estado.clone());
            agente.send(msg);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo empaquetar el estado", e);
        }
    }

    /** ¿El mensaje responde al último REQUEST? (descarta respuestas de búsquedas anteriores) */
    public boolean esRespuestaActual(ACLMessage msg) {
        return idPedido(pedidos).equals(msg.getInReplyTo());
    }

    /** INFORM (camino, nodos expandidos, tiempo) -> GUI principal y GUI del solucionador. */
    public void informarResultado(List<int[]> camino, int expandidos, long tiempoMs) {
        List<String> pasos = new ArrayList<>();
        for (int[] e : camino) {
            pasos.add(Arrays.toString(e));
        }
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(gui);
        msg.addReceiver(guiSolucionador);
        msg.setContent("heuristica=euclidiana;camino=" + String.join(">", pasos)
                + ";expandidos=" + expandidos + ";tiempoMs=" + tiempoMs);
        agente.send(msg);
    }

    private String idPedido(int n) {
        return agente.getLocalName() + "-" + n;
    }
}
