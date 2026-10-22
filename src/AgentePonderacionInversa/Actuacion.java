package AgentePonderacionInversa;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Actuación del solucionador: pedir vecinos al generador e informar el resultado a las GUIs.
 */
public class Actuacion {

    private final Agent agente;
    private final AID generador = new AID(AgentePonderacionInversa.GENERADOR, AID.ISLOCALNAME);
    private final AID gui = new AID(AgentePonderacionInversa.GUI, AID.ISLOCALNAME);
    private final AID guiSolucionador = new AID(AgentePonderacionInversa.GUI_SOLUCIONADOR, AID.ISLOCALNAME);
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
    public void informarResultado(List<int[]> camino, int expandidos, double tiempoMs) {
        List<String> pasos = new ArrayList<>();
        for (int[] e : camino) {
            pasos.add(Arrays.toString(e));
        }
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(gui);
        msg.addReceiver(guiSolucionador);
        msg.setContent("heuristica=ponderacionInversa;camino=" + String.join(">", pasos)
                + ";expandidos=" + expandidos + ";tiempoMs=" + String.format(Locale.ROOT, "%.3f", tiempoMs));
        agente.send(msg);
    }

    // ===================== Depuración (pocas líneas) =====================

    public void imprimirInicio() {
        System.out.println("\n===== NUEVO PUZZLE: buscando con la heurística ponderacion inversa =====");
    }

    /** Solo los 3 primeros nodos elegidos y luego uno cada 100, para no llenar la terminal. */
    public void imprimirAvance(int expandidos, int[] elegido, double h) {
        if (expandidos <= 3 || expandidos % 100 == 0) {
            System.out.println("  #" + expandidos + " elegido " + Arrays.toString(elegido)
                    + "  h = " + String.format(Locale.ROOT, "%.2f", h));
        }
    }

    public void imprimirMeta(int movimientos, int expandidos, double tiempoMs) {
        System.out.println("META ALCANZADA: " + movimientos + " movimientos, " + expandidos + " expandidos, "
                + String.format(Locale.ROOT, "%.3f", tiempoMs) + " ms de procesador");
    }

    public void imprimirSinSolucion() {
        System.out.println("Frontera vacía: sin solución.");
    }

    private String idPedido(int n) {
        return agente.getLocalName() + "-" + n;
    }
}
