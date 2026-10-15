package GUI;

import AgenteGenerador.AgenteGenerador;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import javafx.application.Platform;

import java.io.IOException;
import java.util.List;

/**
 * Actuación del Agente GUI: lo que el agente hace hacia afuera.
 * Mostrar el tablero en la ventana y en la terminal, y enviar el puzzle al generador.
 */
public class Actuacion {

    private final String nombreAgente;
    private Ventana ventana;

    public Actuacion(String nombreAgente) {
        this.nombreAgente = nombreAgente;
    }

    public void setVentana(Ventana ventana) {
        this.ventana = ventana;
    }

    public void mostrarTablero(int[] tablero) {
        imprimirTablero(tablero);
        if (ventana != null) {
            Platform.runLater(() -> ventana.mostrarTablero(tablero)); // la ventana vive en el hilo de JavaFX
        }
    }

    /** Desliza la ficha de 'desde' al hueco ('hacia') e imprime el tablero resultante. */
    public void moverFicha(int desde, int hacia, int[] tableroNuevo) {
        imprimirTablero(tableroNuevo);
        if (ventana != null) {
            Platform.runLater(() -> ventana.animarMovimiento(desde, hacia));
        }
    }

    /**
     * Anima la solución paso a paso (a ritmo visible) desde el tablero inicial
     * y, al terminar la animación, imprime el resumen en la terminal.
     */
    public void reproducirSolucion(int[] inicial, List<int[]> movimientos, String resumen) {
        if (ventana != null) {
            Platform.runLater(() -> ventana.reproducirSolucion(inicial, movimientos,
                    () -> System.out.println(resumen)));
        }
    }

    /** REQUEST al generador con el tablero (int[9]) como puzzle inicial. */
    public void enviarPuzzle(Agent agente, int[] tablero) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID(AgenteGenerador.NOMBRE, AID.ISLOCALNAME));
        try {
            msg.setContentObject(tablero.clone());
            agente.send(msg);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo empaquetar el tablero", e);
        }
    }

    /** Imprime el tablero en la terminal para depurar. */
    private void imprimirTablero(int[] tablero) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<9;i++){
            sb.append(tablero[i]);
        }
        System.out.println(sb);
    }
}
