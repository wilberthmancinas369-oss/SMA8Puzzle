package GUI;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import javafx.application.Platform;

/**
 * Agente GUI: recibe el puzzle del usuario (o propone uno aleatorio),
 * se lo entrega al generador y muestra los resultados.
 * No genera el espacio de estados.
 *
 * Extiende GuiAgent para que el clic de un botón (hilo de JavaFX)
 * llegue al hilo del agente como un GuiEvent: es una percepción.
 */
public class AgenteGUI extends GuiAgent {

    static final int EVENTO_COMENZAR = 1;
    static final int EVENTO_CASILLA = 2;

    private final Percepcion percepcion = new Percepcion();
    private final Razonamiento razonamiento = new Razonamiento();
    private Actuacion actuacion;
    private Ventana ventana;
    private int[] tableroActual;

    @Override
    protected void setup() {
        actuacion = new Actuacion(getLocalName());
        tableroActual = razonamiento.generarTableroAleatorio();
        abrirVentana();
    }

    private void abrirVentana() {
        Runnable crear = () -> {
            ventana = new Ventana(
                    () -> postGuiEvent(new GuiEvent(this, EVENTO_COMENZAR)), // botón -> agente
                    casilla -> {                                              // casilla -> agente
                        GuiEvent evento = new GuiEvent(this, EVENTO_CASILLA);
                        evento.addParameter(casilla);
                        postGuiEvent(evento);
                    },
                    this::doDelete);                                          // cerrar -> terminar agente
            actuacion.setVentana(ventana);
            ventana.mostrar();
            actuacion.mostrarTablero(tableroActual); // tablero inicial
        };
        try {
            Platform.startup(crear);  // primera vez: arranca JavaFX
        } catch (IllegalStateException yaIniciado) {
            Platform.runLater(crear); // JavaFX ya estaba corriendo
        }
    }

    @Override
    protected void onGuiEvent(GuiEvent evento) {
        // PERCEPCIÓN: el usuario presionó "Comenzar" o tocó una casilla
        switch (evento.getType()) {
            case EVENTO_COMENZAR -> comenzar();
            case EVENTO_CASILLA -> tocarCasilla((Integer) evento.getParameter(0));
        }
    }

    private void comenzar() {
        // RAZONAMIENTO: proponer un tablero nuevo que siempre tenga solución
        tableroActual = razonamiento.generarTableroAleatorio();

        // ACTUACIÓN: mostrarlo en la ventana y en la terminal
        actuacion.mostrarTablero(tableroActual);
    }

    private void tocarCasilla(int casilla) {
        // RAZONAMIENTO: ¿la ficha tocada está junto al hueco?
        int hueco = Razonamiento.posicionHueco(tableroActual);
        int[] nuevo = Razonamiento.mover(tableroActual, casilla);
        if (nuevo == null) {
            return; // no se puede mover: se ignora el clic
        }
        tableroActual = nuevo;

        // ACTUACIÓN: deslizar la ficha hacia el hueco e imprimir el tablero
        actuacion.moverFicha(casilla, hueco, tableroActual);
    }

    @Override
    protected void takeDown() {
        if (ventana != null) {
            Platform.runLater(ventana::cerrar);
        }
    }
}
