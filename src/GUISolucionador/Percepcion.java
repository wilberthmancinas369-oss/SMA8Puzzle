package GUISolucionador;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.List;

/**
 * Percepción de la GUI del solucionador: lee el buzón y reconoce
 * el puzzle que manda el generador y el resultado que manda el solucionador.
 */
public class Percepcion {

    /** Lo que reporta un solucionador al encontrar la meta. */
    public record Resultado(String solucionador, String heuristica, List<int[]> camino,
                            int expandidos, double tiempoMs) {
    }

    public ACLMessage percibir(Agent agente) {
        return agente.receive(); // todo el buzón: lo que no se reconozca se descarta
    }

    /** @return el puzzle (int[9]) si el mensaje es el INFORM del generador, o null */
    public int[] leerPuzzle(ACLMessage msg) {
        if (msg.getPerformative() != ACLMessage.INFORM
                || !AgenteGUISolucionador.GENERADOR.equals(msg.getSender().getLocalName())) {
            return null;
        }
        try {
            return msg.getContentObject() instanceof int[] puzzle && puzzle.length == 9 ? puzzle : null;
        } catch (UnreadableException e) {
            return null;
        }
    }

    /**
     * Interpreta "heuristica=...;camino=[..]>[..];expandidos=...;tiempoMs=...".
     * @return el resultado, o null si el mensaje no es un resultado de un solucionador
     */
    public Resultado leerResultado(ACLMessage msg) {
        if (msg.getPerformative() != ACLMessage.INFORM || msg.getContent() == null) {
            return null;
        }
        String heuristica = null;
        List<int[]> camino = null;
        int expandidos = 0;
        double tiempoMs = 0;
        try {
            for (String campo : msg.getContent().split(";")) {
                String[] par = campo.split("=", 2);
                if (par.length != 2) {
                    continue;
                }
                switch (par[0].trim()) {
                    case "heuristica" -> heuristica = par[1].trim();
                    case "camino" -> camino = leerCamino(par[1]);
                    case "expandidos" -> expandidos = Integer.parseInt(par[1].trim());
                    case "tiempoMs" -> tiempoMs = Double.parseDouble(par[1].trim()); // puede traer decimales
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }
        if (heuristica == null || camino == null) {
            return null;
        }
        return new Resultado(msg.getSender().getLocalName(), heuristica, camino, expandidos, tiempoMs);
    }

    /** "[1, 2, 3, ...]>[...]" -> lista de estados int[9] */
    private List<int[]> leerCamino(String texto) {
        List<int[]> camino = new ArrayList<>();
        for (String paso : texto.split(">")) {
            String digitos = paso.replaceAll("[^0-8]", "");
            if (digitos.length() != 9) {
                continue;
            }
            int[] estado = new int[9];
            for (int i = 0; i < 9; i++) {
                estado[i] = digitos.charAt(i) - '0';
            }
            camino.add(estado);
        }
        return camino;
    }
}
