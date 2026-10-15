package GUI;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Percepción del Agente GUI: lee el buzón y reconoce los resultados de los solucionadores.
 * (Los eventos de la ventana llegan aparte, por onGuiEvent.)
 */
public class Percepcion {

    /** Lo que reporta un solucionador al encontrar la meta. */
    public record Resultado(String solucionador, String heuristica, List<int[]> camino,
                            int expandidos, long tiempoMs) {
    }

    public ACLMessage percibir(Agent agente) {
        return agente.receive(); // todo el buzón: lo que no sea un resultado se descarta
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
        long tiempoMs = 0;
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
                    case "tiempoMs" -> tiempoMs = Long.parseLong(par[1].trim());
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
