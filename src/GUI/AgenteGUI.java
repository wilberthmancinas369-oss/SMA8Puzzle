package GUI;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import java.util.Arrays;

/**
 * Agente GUI: recibe el puzzle del usuario (o propone uno aleatorio),
 * se lo entrega al generador y muestra los resultados.
 * No genera el espacio de estados.
 */
public class AgenteGUI extends GuiAgent {

    private final Percepcion percepcion = new Percepcion();
    private final Razonamiento razonamiento = new Razonamiento();
    private final Actuacion actuacion = new Actuacion();

    @Override
    protected void setup() {
        System.out.println("[" + getLocalName() + "] Iniciado.");

        int[] tablero = razonamiento.generarTableroAleatorio();
        System.out.println("[" + getLocalName() + "] Tablero aleatorio: " + Arrays.toString(tablero)
                + " (inversiones: " + Razonamiento.contarInversiones(tablero) + ")");
    }

    @Override
    protected void onGuiEvent(GuiEvent evento) {
        // Pendiente: eventos de la ventana (botones del usuario)
    }
}
