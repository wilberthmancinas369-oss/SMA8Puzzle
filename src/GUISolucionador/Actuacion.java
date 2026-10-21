package GUISolucionador;

import javafx.application.Platform;

import java.util.List;

/**
 * Actuación de la GUI del solucionador: lo que el agente hace hacia afuera.
 * Mostrar el puzzle, animar la solución y escribir el resumen en la terminal.
 */
public class Actuacion {

    private Ventana ventana;

    public void setVentana(Ventana ventana) {
        this.ventana = ventana;
    }

    public void esperarPuzzle() {
        enVentana(() -> ventana.mostrarMensaje("Esperando puzzle…", true));
    }

    public void mostrarPuzzle(int[] puzzle) {
        imprimirTablero(puzzle);
        enVentana(() -> {
            ventana.mostrarTablero(puzzle);
            ventana.mostrarMensaje("Esperando la solución…", true);
        });
    }

    /**
     * Anima la solución paso a paso (a ritmo visible) desde el puzzle
     * y, al terminar, muestra e imprime el resumen.
     */
    public void reproducirSolucion(int[] puzzle, List<int[]> movimientos, Percepcion.Resultado r) {
        String resumen = r.heuristica() + ": " + movimientos.size() + " movimientos · "
                + r.expandidos() + " expandidos · " + r.tiempoMs() + " ms";
        enVentana(() -> {
            ventana.mostrarMensaje("Resolviendo con " + r.heuristica() + "…", false);
            ventana.reproducirSolucion(puzzle, movimientos, () -> {
                ventana.mostrarMensaje(resumen, false);
                System.out.println(r.solucionador() + " (" + resumen + ")");
            });
        });
    }

    /** La ventana vive en el hilo de JavaFX. */
    private void enVentana(Runnable accion) {
        if (ventana != null) {
            Platform.runLater(accion);
        }
    }

    /** Imprime el tablero en la terminal para depurar. */
    private void imprimirTablero(int[] tablero) {
        StringBuilder sb = new StringBuilder();
        for (int n : tablero) {
            sb.append(n);
        }
        System.out.println(sb);
    }
}
