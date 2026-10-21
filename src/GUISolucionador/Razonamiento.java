package GUISolucionador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Razonamiento de la GUI del solucionador: traducir el camino de estados
 * que reporta el solucionador en movimientos de fichas para animarlos.
 *
 * Un tablero es un arreglo de 9 enteros leído por filas, con 0 como espacio vacío.
 */
public class Razonamiento {

    /**
     * Casillas vecinas de cada posición del tablero:
     *  0 | 1 | 2     esquina (0, 2, 6, 8) -> 2 movimientos
     *  3 | 4 | 5     arista  (1, 3, 5, 7) -> 3 movimientos
     *  6 | 7 | 8     centro  (4)          -> 4 movimientos
     */
    public static final int[][] VECINOS = {
            {1, 3},       {0, 2, 4},    {1, 5},
            {0, 4, 6},    {1, 3, 5, 7}, {2, 4, 8},
            {3, 7},       {4, 6, 8},    {5, 7}
    };

    /**
     * Convierte un camino de estados en movimientos {desde, hacia} a partir del tablero inicial.
     * En cada paso, la ficha que se movió estaba donde ahora quedó el hueco.
     * @return los movimientos, o null si algún paso no es un movimiento legal
     */
    public static List<int[]> movimientos(int[] inicial, List<int[]> camino) {
        List<int[]> movimientos = new ArrayList<>();
        int[] actual = inicial;
        for (int[] siguiente : camino) {
            if (Arrays.equals(siguiente, actual)) {
                continue; // por si el camino incluye el estado inicial
            }
            int desde = posicionHueco(siguiente);
            int hacia = posicionHueco(actual);
            if (!Arrays.equals(mover(actual, desde), siguiente)) {
                return null;
            }
            movimientos.add(new int[]{desde, hacia});
            actual = siguiente;
        }
        return movimientos;
    }

    /**
     * Mueve la ficha de la posición 'casilla' hacia el hueco.
     * @return el tablero nuevo, o null si la casilla no es vecina del hueco
     */
    public static int[] mover(int[] tablero, int casilla) {
        int hueco = posicionHueco(tablero);
        for (int vecino : VECINOS[hueco]) {
            if (vecino == casilla) {
                int[] nuevo = tablero.clone();
                nuevo[hueco] = nuevo[casilla];
                nuevo[casilla] = 0;
                return nuevo;
            }
        }
        return null;
    }

    public static int posicionHueco(int[] tablero) {
        for (int i = 0; i < tablero.length; i++) {
            if (tablero[i] == 0) {
                return i;
            }
        }
        throw new IllegalArgumentException("El tablero no tiene hueco");
    }
}
