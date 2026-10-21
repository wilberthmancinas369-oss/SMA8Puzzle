package AgenteManhattan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Razonamiento: Greedy Best-First Search con la heurística manhattan.
 * Un estado es un int[9] por filas, 0 = espacio vacío.
 */
public class Razonamiento {

    public static final int[] META = {1, 2, 3, 4, 5, 6, 7, 8, 0};

    private final int[] posMeta = new int[9]; // posMeta[ficha] = casilla de esa ficha en la meta
    private final PriorityQueue<int[]> abierta = new PriorityQueue<>(Comparator.comparingDouble(this::manhattan));
    private final Set<String> visitados = new HashSet<>();
    private final Map<String, int[]> padre = new HashMap<>(); // para reconstruir el camino
    private int[] actual;
    private int expandidos;
    private long nanosProceso; // tiempo de procesador que lleva esta búsqueda

    public Razonamiento() {
        for (int i = 0; i < 9; i++) {
            posMeta[META[i]] = i;
        }
    }

    /** Puzzle nuevo: los vecinos del estado inicial son el primer nivel de la búsqueda. */
    public void nuevoPuzzle(int[][] vecinos) {
        abierta.clear();
        visitados.clear();
        padre.clear();
        expandidos = 0;
        nanosProceso = 0;
        actual = null;
        recibirVecinos(vecinos);
    }

    /** Agrega a la frontera los vecinos del nodo actual que no se hayan visto. */
    public void recibirVecinos(int[][] vecinos) {
        for (int[] estado : vecinos) {
            if (visitados.add(clave(estado))) {
                padre.put(clave(estado), actual);
                abierta.add(estado);
            }
        }
    }

    /** Saca de la frontera el estado con menor h. Devuelve null si ya no hay estados. */
    public int[] siguiente() {
        actual = abierta.poll();
        if (actual == null) {
            return null;
        }
        if (!esMeta(actual)) {
            expandidos++;
        }
        return actual;
    }

    public boolean esMeta(int[] estado) {
        return Arrays.equals(estado, META);
    }

    /** Estados desde el primer movimiento hasta 'fin', siguiendo a los padres. */
    public List<int[]> camino(int[] fin) {
        List<int[]> ruta = new ArrayList<>();
        for (int[] e = fin; e != null; e = padre.get(clave(e))) {
            ruta.add(e);
        }
        Collections.reverse(ruta);
        return ruta;
    }

    /** h(n): suma de las distancias Manhattan de cada ficha a su casilla meta. */
    public double manhattan(int[] estado) {
        double total = 0;
        for (int i = 0; i < 9; i++) {
            int ficha = estado[i];
            if (ficha == 0) {
                continue;
            }
            int j = posMeta[ficha];
            int dx = j % 3 - i % 3;
            int dy = j / 3 - i / 3;
            total += Math.abs(dx) + Math.abs(dy);
        }
        return total;
    }

    public int getExpandidos() {
        return expandidos;
    }

    /**
     * Suma el tiempo de un turno del solucionador (percibir, razonar y actuar).
     * No cuenta la espera de los vecinos del generador ni lo que se imprime.
     */
    public void sumarTiempo(long nanos) {
        nanosProceso += nanos;
    }

    /** Tiempo de procesador de la búsqueda, en milisegundos con decimales. */
    public double getTiempoMs() {
        return nanosProceso / 1_000_000.0;
    }

    private static String clave(int[] estado) {
        return Arrays.toString(estado);
    }
}
