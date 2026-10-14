package GUI;

import javafx.application.Platform;

/**
 * Actuación del Agente GUI: lo que el agente hace hacia afuera.
 * Por ahora: mostrar el tablero en la ventana y en la terminal.
 * Pendiente: enviar el puzzle al generador (REQUEST).
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

    /** Imprime el tablero en la terminal para depurar. */
    private void imprimirTablero(int[] tablero) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<9;i++){
            sb.append(tablero[i]);
        }
        System.out.println(sb);
    }
}
