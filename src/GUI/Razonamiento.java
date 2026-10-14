package GUI;

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
