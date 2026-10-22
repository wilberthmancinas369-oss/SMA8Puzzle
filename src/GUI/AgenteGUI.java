package GUI;

import AgenteGenerador.AgenteGenerador;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agente GUI: recibe el puzzle del usuario (o propone uno aleatorio),
 * se lo entrega al generador y, cuando los 3 solucionadores terminan, los compara.
 * La animación de cada solución se ve en la GUI de cada solucionador (gui1, gui2, gui3).
 * No genera el espacio de estados.
 *
 * Extiende GuiAgent para que el clic de un botón (hilo de JavaFX)
 * llegue al hilo del agente como un GuiEvent: es una percepción.
 */
public class AgenteGUI extends GuiAgent {

    static final int EVENTO_COMENZAR = 1;
    static final int EVENTO_CASILLA = 2;
    static final int EVENTO_RESOLVER = 3;

    private static final List<String> SOLUCIONADORES = Arrays.asList(AgenteGenerador.SOLUCIONADORES);
    private static final int MENSAJES_RESULTADO = 2; // cada solucionador manda su INFORM a gui y a su guiN

    private final Percepcion percepcion = new Percepcion();
    private final Razonamiento razonamiento = new Razonamiento();
    private Actuacion actuacion;
    private Ventana ventana;
    private int[] tableroActual;
    private int[] tableroResuelto;  // el tablero que se mandó a resolver
    private boolean esperando = false;
    private final Map<String, Ventana.Fila> resultados = new HashMap<>(); // por solucionador

    @Override
    protected void setup() {
        actuacion = new Actuacion(getLocalName());
        tableroActual = razonamiento.generarTableroAleatorio();
        abrirVentana();
        addBehaviour(new EscucharSolucionadores());
    }

    private void abrirVentana() {
        Runnable crear = () -> {
            ventana = new Ventana(
                    () -> postGuiEvent(new GuiEvent(this, EVENTO_COMENZAR)), // botón -> agente
                    () -> postGuiEvent(new GuiEvent(this, EVENTO_RESOLVER)), // botón -> agente
                    casilla -> {                                              // casilla -> agente
                        GuiEvent evento = new GuiEvent(this, EVENTO_CASILLA);
                        evento.addParameter(casilla);
                        postGuiEvent(evento);
                    },
                    this::doDelete);                                          // cerrar -> terminar agente
            actuacion.setVentana(ventana);
            ventana.mostrar();
            actuacion.mostrarTablero(tableroActual); // tablero inicial
            mostrarDistancias();
        };
        try {
            Platform.startup(crear);  // primera vez: arranca JavaFX
        } catch (IllegalStateException yaIniciado) {
            Platform.runLater(crear); // JavaFX ya estaba corriendo
        }
    }

    @Override
    protected void onGuiEvent(GuiEvent evento) {
        // PERCEPCIÓN: el usuario presionó "Comenzar", "Resolver" o tocó una casilla
        switch (evento.getType()) {
            case EVENTO_COMENZAR -> comenzar();
            case EVENTO_RESOLVER -> resolver();
            case EVENTO_CASILLA -> tocarCasilla((Integer) evento.getParameter(0));
        }
    }

    private void resolver() {
        if (esperando) {
            return;
        }
        // RAZONAMIENTO: recordar qué tablero se va a resolver y empezar a esperar
        tableroResuelto = tableroActual.clone();
        resultados.clear();
        esperando = true;

        // ACTUACIÓN: ocultar el tablero y entregarlo al generador (él lo evalúa)
        actuacion.mostrarEspera(0, SOLUCIONADORES.size());
        actuacion.enviarPuzzle(this, tableroActual);
    }

    /** Resultados que mandan los solucionadores al llegar a la meta. */
    private class EscucharSolucionadores extends CyclicBehaviour {
        @Override
        public void action() {
            // PERCEPCIÓN: un INFORM con el camino de algún solucionador,
            // o la respuesta del generador con los mensajes de cada uno
            ACLMessage msg = percepcion.percibir(myAgent);
            if (msg == null) {
                block();
                return;
            }
            Map<String, Integer> mensajes = percepcion.leerMensajes(msg);
            if (mensajes != null) {
                compararSiEstanTodos(mensajes);
                return;
            }
            Percepcion.Resultado resultado = percepcion.leerResultado(msg);
            if (resultado == null || !esperando) {
                return; // AGREE/REFUSE del generador u otro mensaje
            }

            // RAZONAMIENTO: ¿es de un solucionador y su camino sale del tablero que mandé?
            int color = SOLUCIONADORES.indexOf(resultado.solucionador());
            List<int[]> movimientos = Razonamiento.movimientos(tableroResuelto, resultado.camino());
            if (color < 0 || movimientos == null) {
                return; // otro agente, o el resultado de un puzzle anterior
            }
            resultados.put(resultado.solucionador(), new Ventana.Fila(resultado.solucionador(),
                    resultado.heuristica(), color, movimientos.size(), resultado.expandidos(), 0, resultado.tiempoMs()));

            // ACTUACIÓN: seguir esperando o, si ya están los 3, preguntar al generador cuántos mensajes hubo
            actuacion.mostrarEspera(resultados.size(), SOLUCIONADORES.size());
            if (resultados.size() == SOLUCIONADORES.size()) {
                actuacion.pedirMensajes(myAgent);
            }
        }
    }

    /** Con los 3 resultados y los mensajes que contó el generador, muestra la comparación. */
    private void compararSiEstanTodos(Map<String, Integer> mensajes) {
        // RAZONAMIENTO: ¿seguimos esperando este puzzle y ya llegaron los 3?
        if (!esperando || resultados.size() < SOLUCIONADORES.size()) {
            return; // respuesta de un puzzle anterior
        }
        esperando = false;
        List<Ventana.Fila> filas = new ArrayList<>();
        for (String nombre : SOLUCIONADORES) { // siempre en el mismo orden
            int total = mensajes.getOrDefault(nombre, 0) + MENSAJES_RESULTADO;
            filas.add(resultados.get(nombre).conMensajes(total));
        }

        // ACTUACIÓN: comparar a los solucionadores
        actuacion.mostrarComparacion(filas,
                Razonamiento.fichasFueraDeLugar(tableroResuelto), Razonamiento.distanciaManhattan(tableroResuelto));
    }

    private void comenzar() {
        // RAZONAMIENTO: proponer un tablero nuevo que siempre tenga solución
        esperando = false; // si se estaba esperando, se cancela
        tableroActual = razonamiento.generarTableroAleatorio();

        // ACTUACIÓN: mostrarlo en la ventana y en la terminal
        actuacion.mostrarTablero(tableroActual);
        mostrarDistancias();
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
        mostrarDistancias();
    }

    /** Qué tan lejos de la meta está el tablero actual (para elegir retos y llenar la tabla). */
    private void mostrarDistancias() {
        actuacion.mostrarDistancias(Razonamiento.fichasFueraDeLugar(tableroActual),
                Razonamiento.distanciaManhattan(tableroActual));
    }

    @Override
    protected void takeDown() {
        if (ventana != null) {
            Platform.runLater(ventana::cerrar);
        }
    }
}
