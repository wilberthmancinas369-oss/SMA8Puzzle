package AgenteGenerador;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Agente Generador del espacio de soluciones.
 * No busca la solución: solo valida el puzzle y genera los estados siguientes
 * de cada nodo que le piden (generación bajo demanda).
 */
public class AgenteGenerador extends Agent {

    public static final String NOMBRE = "generador";
    static final String GUI = "gui";
    public static final String[] SOLUCIONADORES = {"solucionador1", "solucionador2", "solucionador3"};
    static final String[] GUIS_SOLUCIONADORES = {"gui1", "gui2", "gui3"}; // animan cada solución

    private final Percepcion percepcion = new Percepcion();
    private final Razonamiento razonamiento = new Razonamiento();
    private final Actuacion actuacion = new Actuacion();

    @Override
    protected void setup() {
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // PERCEPCIÓN: un REQUEST con un nodo (de la GUI o de un solucionador)
                // o la GUI preguntando cuántos mensajes hubo
                ACLMessage msg = percepcion.percibir(myAgent);
                if (msg == null) {
                    block();
                    return;
                }
                if (percepcion.esConsultaMensajes(msg)) {
                    actuacion.informarMensajes(myAgent, msg, razonamiento.resumenMensajes());
                    return;
                }
                int[] nodo = percepcion.leerNodo(msg);

                // RAZONAMIENTO: ¿el puzzle tiene solución? ¿cuáles son sus estados siguientes?
                if (!razonamiento.esValido(nodo)) {
                    actuacion.rechazar(myAgent, msg);
                    return;
                }
                int[][] vecinos = razonamiento.generarVecinos(nodo);

                // ACTUACIÓN: entregar los vecinos
                actuacion.imprimirVecinos(vecinos);
                if (msg.getSender().getLocalName().equals(GUI)) {
                    razonamiento.reiniciarMensajes(SOLUCIONADORES);
                    actuacion.aceptar(myAgent, msg);                                  // AGREE a la GUI
                    actuacion.entregarASolucionadores(myAgent, SOLUCIONADORES, vecinos); // puzzle nuevo
                    actuacion.avisarGuis(myAgent, GUIS_SOLUCIONADORES, nodo);             // qué puzzle animar
                } else {
                    razonamiento.contarPedido(msg.getSender().getLocalName());
                    actuacion.responder(myAgent, msg, vecinos);                       // nodo elegido por un solucionador
                }
            }
        });
    }
}
