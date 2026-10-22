package AgenteGenerador;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Razonamiento del Agente Generador: las reglas del 8-Puzzle.
 * Un nodo es un arreglo de 9 enteros leído por filas, con 0 como espacio vacío.
 */
public class Razonamiento {

    /**
     * Casillas vecinas de cada posición del tablero:
     *  0 | 1 | 2     esquina (0, 2, 6, 8) -> 2 movimientos
     *  3 | 4 | 5     arista  (1, 3, 5, 7) -> 3 movimientos
     *  6 | 7 | 8     centro  (4)          -> 4 movimientos
     */
    private static final int[][] VECINOS = {
            {1, 3},       {0, 2, 4},    {1, 5},
            {0, 4, 6},    {1, 3, 5, 7}, {2, 4, 8},
            {3, 7},       {4, 6, 8},    {5, 7}
    };

    /** Mensajes FIPA que cada solucionador ha intercambiado con el generador en el puzzle actual. */
    private final Map<String, Integer> mensajes = new LinkedHashMap<>();

    /** Puzzle nuevo: cada solucionador empieza con el INFORM que trae los primeros vecinos. */
    public void reiniciarMensajes(String[] solucionadores) {
        mensajes.clear();
        for (String nombre : solucionadores) {
            mensajes.put(nombre, 1);
        }
    }

    /** Un solucionador pidió vecinos: su REQUEST y el INFORM de respuesta. */
    public void contarPedido(String solucionador) {
        mensajes.merge(solucionador, 2, Integer::sum);
    }

    /** "solucionador1=N;solucionador2=N;solucionador3=N" */
    public String resumenMensajes() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : mensajes.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append(';');
            }
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        return sb.toString();
    }

    /** Estados siguientes: mover el hueco a cada casilla vecina (2, 3 o 4 nodos). */
    public int[][] generarVecinos(int[] nodo) {
        int hueco = posicionHueco(nodo);
        int[][] vecinos = new int[VECINOS[hueco].length][];
        for (int i = 0; i < vecinos.length; i++) {
            int casilla = VECINOS[hueco][i];
            int[] vecino = nodo.clone();
            vecino[hueco] = vecino[casilla];
            vecino[casilla] = 0;
            vecinos[i] = vecino;
        }
        return vecinos;
    }

    /**
     * Un nodo es válido si tiene exactamente los números 0..8 (sin repetir)
     * y su número de inversiones es par (si no, el puzzle no tiene solución).
     */
    public boolean esValido(int[] nodo) {
        if (nodo == null || nodo.length != 9) {
            return false;
        }
        boolean[] visto = new boolean[9];
        for (int n : nodo) {
            if (n < 0 || n > 8 || visto[n]) {
                return false;
            }
            visto[n] = true;
        }
        int inversiones = 0;
        for (int i = 0; i < nodo.length; i++) {
            for (int j = i + 1; j < nodo.length; j++) {
                if (nodo[i] != 0 && nodo[j] != 0 && nodo[i] > nodo[j]) {
                    inversiones++;
                }
            }
        }
        return inversiones % 2 == 0;
    }

    private int posicionHueco(int[] nodo) {
        for (int i = 0; i < nodo.length; i++) {
            if (nodo[i] == 0) {
                return i;
            }
        }
        throw new IllegalArgumentException("El nodo no tiene hueco");
    }
}
