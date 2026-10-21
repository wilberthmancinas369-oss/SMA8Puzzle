package AgenteManhattan;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.List;

/**
 * Solucionador 3: Greedy Best-First Search con la heurística manhattan.
 * Se lanza con el nombre "solucionador3" (así lo busca el generador).
 */
public class AgenteManhattan extends Agent {

    static final String GENERADOR = "generador";
    static final String GUI = "gui";              // GUI principal: compara a los 3 solucionadores
    static final String GUI_SOLUCIONADOR = "gui2"; // su propia GUI: anima la solución

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
                long inicio = System.nanoTime(); // desde aquí el solucionador usa el procesador
                int[][] vecinos = percepcion.leerVecinos(msg);
                if (vecinos == null) {
                    return;
                }

                // RAZONAMIENTO: agregar los estados a la frontera y elegir el de menor h
                boolean nuevo = percepcion.esNuevoPuzzle(msg);
                if (nuevo) {
                    razonamiento.nuevoPuzzle(vecinos);
                } else if (actuacion.esRespuestaActual(msg)) {
                    razonamiento.recibirVecinos(vecinos);
                } else {
                    return; // respuesta de una búsqueda anterior
                }
                int[] elegido = razonamiento.siguiente();
                if (elegido == null) {
                    actuacion.imprimirSinSolucion(); // frontera vacía
                    return;
                }

                // ACTUACIÓN: si es la meta, reportar a las GUIs; si no, pedir sus vecinos
                // (el reloj se detiene antes de imprimir, para que la terminal no infle el tiempo)
                if (razonamiento.esMeta(elegido)) {
                    List<int[]> camino = razonamiento.camino(elegido);
                    razonamiento.sumarTiempo(System.nanoTime() - inicio);
                    actuacion.informarResultado(camino, razonamiento.getExpandidos(), razonamiento.getTiempoMs());
                    if (nuevo) {
                        actuacion.imprimirInicio();
                    }
                    actuacion.imprimirMeta(camino.size(), razonamiento.getExpandidos(), razonamiento.getTiempoMs());
                } else {
                    actuacion.pedirVecinos(elegido);
                    razonamiento.sumarTiempo(System.nanoTime() - inicio);
                    if (nuevo) {
                        actuacion.imprimirInicio();
                    }
                    actuacion.imprimirAvance(razonamiento.getExpandidos(), elegido, razonamiento.manhattan(elegido));
                }
            }
        });
    }
}
