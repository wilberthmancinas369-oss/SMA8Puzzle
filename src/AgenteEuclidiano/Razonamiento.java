package AgenteEuclidiano;

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
 * Razonamiento: Greedy Best-First Search con la heurística euclidiana.
 * Un estado es un int[9] por filas, 0 = espacio vacío.
 */
public class Razonamiento {

    public static final int[] META = {1, 2, 3, 4, 5, 6, 7, 8, 0};

    private final int[] posMeta = new int[9]; // posMeta[ficha] = casilla de esa ficha en la meta
    private final PriorityQueue<int[]> abierta = new PriorityQueue<>(Comparator.comparingDouble(this::euclidiana));
    private final Set<String> visitados = new HashSet<>();
    private final Map<String, int[]> padre = new HashMap<>(); // para reconstruir el camino
    private int[] actual;
    private int expandidos;
    private long inicioNanos;
    private long tiempoMs;

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
        inicioNanos = System.nanoTime();
        actual = null;
        recibirVecinos(vecinos);
    }

    /** Agrega a la frontera los vecinos del nodo actual que no se hayan visto. */
    public void recibirVecinos(int[][] vecinos) {
        System.out.println("\nEvaluando vecinos del nodo actual...");
        for (int[] estado : vecinos) {
            double h = euclidiana(estado);
            System.out.println("   - Vecino " + Arrays.toString(estado) + " -> h = " + String.format("%.2f", h));
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
            System.out.println("No hay más nodos en la frontera. Sin solución.");
            return null;
        }
        
        System.out.println("Elegido el nodo con menor h: " + Arrays.toString(actual) + " (h = " + String.format("%.2f", euclidiana(actual)) + ")");
        
        if (esMeta(actual)) {
            System.out.println("META ALCANZADA!");
            tiempoMs = (System.nanoTime() - inicioNanos) / 1_000_000;
        } else {
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

    /** h(n): suma de las distancias en línea recta de cada ficha a su casilla meta. */
    public double euclidiana(int[] estado) {
        double total = 0;
        for (int i = 0; i < 9; i++) {
            int ficha = estado[i];
            if (ficha == 0) {
                continue;
            }
            int j = posMeta[ficha];
            int dx = j % 3 - i % 3;
            int dy = j / 3 - i / 3;
            total += Math.sqrt(dx * dx + dy * dy);
        }
        return total;
    }

    public int getExpandidos() {
        return expandidos;
    }

    public long getTiempoMs() {
        return tiempoMs;
    }

    private static String clave(int[] estado) {
        return Arrays.toString(estado);
    }
}
