package GUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Razonamiento del Agente GUI.
 * Por ahora: proponer tableros aleatorios que siempre tengan solución.
 *
 * Un tablero es un arreglo de 9 enteros leído por filas, con 0 como espacio vacío.
 * Ejemplo: {1,2,3,4,0,6,7,5,8}
 */
public class Razonamiento {

    public static final int[] META = {1, 2, 3, 4, 5, 6, 7, 8, 0};

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

    private final Random rnd = new Random();

    /**
     * Revuelve las fichas al azar y repite hasta obtener un tablero válido.
     * La mitad de las permutaciones tienen solución, así que en promedio
     * se necesitan solo 2 intentos.
     */
    public int[] generarTableroAleatorio() {
        int[] tablero;
        do {
            tablero = mezclar();
        } while (!esValido(tablero));
        return tablero;
    }

    /** Permutación aleatoria de 0..8 (algoritmo de Fisher-Yates: sin repetidos). */
    private int[] mezclar() {
        int[] tablero = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        for (int i = tablero.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1); // posición al azar entre 0 e i
            int tmp = tablero[i];
            tablero[i] = tablero[j];
            tablero[j] = tmp;
        }
        return tablero;
    }

    /**
     * Intenta mover la ficha de la posición 'casilla' hacia el hueco.
     * Solo es posible si la casilla es vecina del hueco.
     * @return el tablero nuevo, o null si esa ficha no se puede mover
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

    /** Fichas (sin contar el hueco) que no están en su casilla de la meta. */
    public static int fichasFueraDeLugar(int[] tablero) {
        int fuera = 0;
        for (int i = 0; i < tablero.length; i++) {
            if (tablero[i] != 0 && tablero[i] != META[i]) {
                fuera++;
            }
        }
        return fuera;
    }

    /** Suma, por ficha, de las filas y columnas que la separan de su casilla en la meta. */
    public static int distanciaManhattan(int[] tablero) {
        int distancia = 0;
        for (int i = 0; i < tablero.length; i++) {
            int n = tablero[i];
            if (n != 0) {
                int meta = n - 1; // en la meta, la ficha n está en la casilla n-1
                distancia += Math.abs(i / 3 - meta / 3) + Math.abs(i % 3 - meta % 3);
            }
        }
        return distancia;
    }

    public static int posicionHueco(int[] tablero) {
        for (int i = 0; i < tablero.length; i++) {
            if (tablero[i] == 0) {
                return i;
            }
        }
        throw new IllegalArgumentException("El tablero no tiene hueco");
    }

    /**
     * Un tablero es válido si tiene exactamente los números 0..8 (sin repetir)
     * y su número de inversiones es par (regla de solubilidad del 8-Puzzle).
     */
    public static boolean esValido(int[] tablero) {
        if (tablero == null || tablero.length != 9) {
            return false;
        }
        boolean[] visto = new boolean[9];
        for (int n : tablero) {
            if (n < 0 || n > 8 || visto[n]) {
                return false; // número fuera de rango o repetido
            }
            visto[n] = true;
        }
        return contarInversiones(tablero) % 2 == 0;
    }

    /**
     * Cuenta las parejas de fichas (sin el 0) en las que un número mayor
     * aparece antes que uno menor al leer el tablero por filas.
     */
    public static int contarInversiones(int[] tablero) {
        int inversiones = 0;
        for (int i = 0; i < tablero.length; i++) {
            for (int j = i + 1; j < tablero.length; j++) {
                if (tablero[i] != 0 && tablero[j] != 0 && tablero[i] > tablero[j]) {
                    inversiones++;
                }
            }
        }
        return inversiones;
    }
}
