package AgenteGenerador;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

/**
 * Actuación del Agente Generador: responder a la GUI y entregar
 * los estados siguientes (int[][]) a los solucionadores.
 */
public class Actuacion {

    public void aceptar(Agent agente, ACLMessage pedido) {
        ACLMessage respuesta = pedido.createReply();
        respuesta.setPerformative(ACLMessage.AGREE);
        agente.send(respuesta);
    }

    public void rechazar(Agent agente, ACLMessage pedido) {
        ACLMessage respuesta = pedido.createReply();
        respuesta.setPerformative(ACLMessage.REFUSE);
        respuesta.setContent("puzzle sin solución");
        agente.send(respuesta);
    }

    /** Puzzle nuevo: los mismos vecinos van a los tres solucionadores. */
    public void entregarASolucionadores(Agent agente, String[] solucionadores, int[][] vecinos) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        for (String nombre : solucionadores) {
            msg.addReceiver(new AID(nombre, AID.ISLOCALNAME));
        }
        enviar(agente, msg, vecinos);
    }

    /** Puzzle nuevo: las GUIs de los solucionadores lo muestran mientras esperan su solución. */
    public void avisarGuis(Agent agente, String[] guis, int[] puzzle) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        for (String nombre : guis) {
            msg.addReceiver(new AID(nombre, AID.ISLOCALNAME));
        }
        try {
            msg.setContentObject(puzzle.clone());
            agente.send(msg);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo empaquetar el puzzle", e);
        }
    }

    /** Un solucionador eligió un nodo: solo él recibe los vecinos. */
    public void responder(Agent agente, ACLMessage pedido, int[][] vecinos) {
        ACLMessage respuesta = pedido.createReply();
        respuesta.setPerformative(ACLMessage.INFORM);
        enviar(agente, respuesta, vecinos);
    }

    /** INFORM a la GUI con los mensajes de cada solucionador: "solucionador1=N;solucionador2=N;...". */
    public void informarMensajes(Agent agente, ACLMessage consulta, String conteo) {
        ACLMessage respuesta = consulta.createReply(); // conserva el conversation-id "mensajes"
        respuesta.setPerformative(ACLMessage.INFORM);
        respuesta.setContent(conteo);
        agente.send(respuesta);
    }

    /** Imprime cada vecino en una línea para depurar. */
    public void imprimirVecinos(int[][] vecinos) {
        StringBuilder sb = new StringBuilder();
        for (int[] vecino : vecinos) {
            for (int n : vecino) {
                sb.append(n);
            }
            sb.append('\n');
        }
        System.out.println("-------------------------------------");
        System.out.print(sb);
    }

    private void enviar(Agent agente, ACLMessage msg, int[][] vecinos) {
        try {
            msg.setContentObject(vecinos);
            agente.send(msg);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron empaquetar los vecinos", e);
        }
    }
}
