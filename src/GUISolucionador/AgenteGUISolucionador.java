package GUISolucionador;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import javafx.application.Platform;

import java.util.List;

/**
 * GUI de un solucionador: muestra el puzzle que se está resolviendo
 * y anima, paso a paso, la solución que encontró su heurística.
 *
 * Se lanza con el nombre gui1, gui2 o gui3 (el mismo número que su solucionador)
 * y, entre paréntesis, el nombre que se muestra en la ventana: gui3:...(Euclidiana).
 * No envía nada: solo percibe dos mensajes.
 *  - INFORM del generador con el puzzle (int[9])           -> lo muestra y espera
 *  - INFORM del solucionador con el camino a la meta (texto) -> lo anima
 */
public class AgenteGUISolucionador extends Agent {

    static final String GENERADOR = "generador";

    private final Percepcion percepcion = new Percepcion();
    private final Actuacion actuacion = new Actuacion();
    private Ventana ventana;
    private int[] puzzle; // el puzzle que se está resolviendo

    @Override
    protected void setup() {
        abrirVentana();
        addBehaviour(new EscucharMensajes());
    }

    private void abrirVentana() {
        Runnable crear = () -> {
            Object[] args = getArguments(); // lo que va entre paréntesis en -agents, ej. gui3:...(Euclidiana)
            String titulo = args != null && args.length > 0 ? args[0].toString() : getLocalName();
            ventana = new Ventana(titulo, this::doDelete); // cerrar -> terminar agente
            actuacion.setVentana(ventana);
            ventana.mostrar();
            actuacion.esperarPuzzle();
        };
        try {
            Platform.startup(crear);  // primera vez: arranca JavaFX
        } catch (IllegalStateException yaIniciado) {
            Platform.runLater(crear); // JavaFX ya estaba corriendo
        }
    }

    private class EscucharMensajes extends CyclicBehaviour {
        @Override
        public void action() {
            // PERCEPCIÓN: el puzzle (del generador) o el resultado (del solucionador)
            ACLMessage msg = percepcion.percibir(myAgent);
            if (msg == null) {
                block();
                return;
            }
            int[] nuevo = percepcion.leerPuzzle(msg);
            if (nuevo != null) {
                puzzle = nuevo;
                actuacion.mostrarPuzzle(puzzle);
                return;
            }
            Percepcion.Resultado resultado = percepcion.leerResultado(msg);
            if (resultado == null || puzzle == null) {
                return; // otro mensaje
            }

            // RAZONAMIENTO: convertir el camino en movimientos de fichas
            List<int[]> movimientos = Razonamiento.movimientos(puzzle, resultado.camino());
            if (movimientos == null) {
                return; // el camino no sale de este puzzle o tiene pasos ilegales
            }

            // ACTUACIÓN: animar la solución y al final mostrar el resumen
            actuacion.reproducirSolucion(puzzle, movimientos, resultado);
        }
    }

    @Override
    protected void takeDown() {
        if (ventana != null) {
            Platform.runLater(ventana::cerrar);
        }
    }
}
