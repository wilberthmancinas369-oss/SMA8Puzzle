package AgentePonderacionInversa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Razonamiento {

    public static final int[] META = {1, 2, 3, 4, 5, 6, 7, 8, 0};

    /** Nodo de la frontera: g = movimientos desde el inicio, f = g + h. */
    private static class Nodo {
        final int[] estado;
        final int g;
        final double f;

        Nodo(int[] estado, int g, double h) {
            this.estado = estado;
            this.g = g;
            this.f = g + h;
        }
    }

    private final PriorityQueue<Nodo> abierta =
            new PriorityQueue<>(Comparator.comparingDouble((Nodo n) -> n.f));
    private final Map<String, Integer> mejorG = new HashMap<>();  // mejor g conocido por estado
    private final Map<String, int[]> padre = new HashMap<>();     // para reconstruir el camino
    private int[] actual;
    private int gActual;       // g del nodo que se está expandiendo
    private int expandidos;
    private long nanosProceso; // tiempo de procesador que lleva esta búsqueda

    /**
     * Puzzle nuevo: el inicial entra con g = 0 y sus vecinos son el primer nivel.
     *
     * @return false si el puzzle no tiene solución (no se inicia la búsqueda).
     */
    public boolean nuevoPuzzle(int[][] vecinos) {
        abierta.clear();
        mejorG.clear();
        padre.clear();
        expandidos = 0;
        nanosProceso = 0;
        actual = null;
        gActual = 0;

        recibirVecinos(vecinos);
        return true;
    }

    /**
     * Agrega a la frontera los vecinos del nodo actual. Un vecino entra si es nuevo
     * o si se llegó a él por un camino más corto que el conocido.
     */
    public void recibirVecinos(int[][] vecinos) {
        int g = gActual + 1;
        for (int[] original : vecinos) {
            int[] estado = original.clone(); // por si el emisor reutiliza el arreglo
            String k = clave(estado);
            if (g < mejorG.getOrDefault(k, Integer.MAX_VALUE)) {
                mejorG.put(k, g);
                padre.put(k, actual);
                abierta.add(new Nodo(estado, g, ponderacionInversa(estado)));
            }
        }
    }

    /** Saca de la frontera el estado con menor f. Devuelve null si ya no hay estados. */
    public int[] siguiente() {
        Nodo n;
        do {
            n = abierta.poll();
            if (n == null) {
                actual = null;
                return null;
            }
            // Entrada obsoleta: el estado se reinsertó con mejor g.
        } while (n.g > mejorG.get(clave(n.estado)));

        actual = n.estado;
        gActual = n.g;
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

    /** Cálculo de heurística: inversión ponderada (el hueco cuenta como ficha 9). */
    public double ponderacionInversa(int[] estado) {
        double total = 0;
        for (int i = 0; i < 9; i++) {
            int ficha = estado[i] == 0 ? 9 : estado[i];

            for (int j = i + 1; j < 9; j++) {
                int fichaJ = estado[j] == 0 ? 9 : estado[j];

                if (fichaJ > ficha) continue;

                total += ficha - fichaJ;
            }
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