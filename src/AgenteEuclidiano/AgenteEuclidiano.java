package AgenteEuclidiano;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Solucionador 3: Greedy Best-First Search con la heurística euclidiana.
 * Se lanza con el nombre "solucionador3" (así lo busca el generador).
 */
public class AgenteEuclidiano extends Agent {

    static final String GENERADOR = "generador";
    static final String GUI = "gui";

    private final Percepcion percepcion = new Percepcion();
    private final Razonamiento razonamiento = new Razonamiento();
    private Actuacion actuacion;

    @Override
    protected void setup() {
        actuacion = new Actuacion(this);

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // PERCEPCIÓN: INFORM del generador con 2, 3 o 4 estados
                ACLMessage msg = percepcion.percibir(myAgent);
                if (msg == null) {
                    block();
                    return;
                }
                int[][] vecinos = percepcion.leerVecinos(msg);
                if (vecinos == null) {
                    return;
                }

                // RAZONAMIENTO: agregar los estados a la frontera y elegir el de menor h
                if (percepcion.esNuevoPuzzle(msg)) {
                    razonamiento.nuevoPuzzle(vecinos);
                } else if (actuacion.esRespuestaActual(msg)) {
                    razonamiento.recibirVecinos(vecinos);
                } else {
                    return; // respuesta de una búsqueda anterior
                }
                int[] elegido = razonamiento.siguiente();
                if (elegido == null) {
                    return; // frontera vacía: sin solución
                }

                // ACTUACIÓN: si es la meta, reportar a la GUI; si no, pedir sus vecinos
                if (razonamiento.esMeta(elegido)) {
                    actuacion.informarResultado(razonamiento.camino(elegido),
                            razonamiento.getExpandidos(), razonamiento.getTiempoMs());
                } else {
                    actuacion.pedirVecinos(elegido);
                }
            }
        });
    }
}
