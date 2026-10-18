package GUISolucionador.styles;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Genera texturas de madera por código (sin archivos de imagen).
 *
 * Idea: la madera son franjas (vetas) casi horizontales que se ondulan.
 * Se dibujan franjas con una función seno y se deforman con "ruido",
 * luego se agregan fibras finas para que no se vea artificial.
 */
public final class TexturaMadera {

    public static final int TAM = 512;

    /** Arce: madera clara para las fichas. */
    public static final Image ARCE = generar(Color.web("#e8c48f"), Color.web("#b8834b"), 7);

    /** Nogal: madera oscura para el marco. */
    public static final Image NOGAL = generar(Color.web("#7a4a26"), Color.web("#3e2210"), 23);

    private TexturaMadera() {
    }

    private static Image generar(Color claro, Color oscuro, long semilla) {
        WritableImage img = new WritableImage(TAM, TAM);
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < TAM; y++) {
            for (int x = 0; x < TAM; x++) {
                double nx = (double) x / TAM;
                double ny = (double) y / TAM;

                // vetas: franjas horizontales onduladas por el ruido
                double ondulacion = ruido(nx * 3, ny * 6, semilla, 4);
                double veta = Math.sin((ny * 18 + ondulacion * 3.5) * Math.PI);
                veta = Math.pow(Math.abs(veta), 0.6);

                // fibras: rayas finas y largas a lo largo de la madera
                double fibra = ruido(nx * 4, ny * 160, semilla + 1, 3);

                // manchas grandes y suaves
                double mancha = ruido(nx * 2, ny * 2, semilla + 2, 2);

                double t = 0.55 * (1 - veta) + 0.30 * fibra + 0.15 * mancha;
                pw.setColor(x, y, claro.interpolate(oscuro, limitar(t)));
            }
        }
        return img;
    }

    /** Ruido suave de valores (suma de varias escalas), entre 0 y 1. */
    private static double ruido(double x, double y, long semilla, int octavas) {
        double suma = 0, amplitud = 1, total = 0;
        for (int o = 0; o < octavas; o++) {
            int periodo = 1 << o;
            suma += amplitud * ruidoBase(x * periodo, y * periodo, semilla + o);
            total += amplitud;
            amplitud *= 0.5;
        }
        return suma / total;
    }

    private static double ruidoBase(double x, double y, long semilla) {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        double fx = suavizar(x - x0);
        double fy = suavizar(y - y0);
        double a = azar(x0, y0, semilla);
        double b = azar(x0 + 1, y0, semilla);
        double c = azar(x0, y0 + 1, semilla);
        double d = azar(x0 + 1, y0 + 1, semilla);
        double arriba = a + (b - a) * fx;
        double abajo = c + (d - c) * fx;
        return arriba + (abajo - arriba) * fy;
    }

    /** Número pseudoaleatorio fijo para cada punto (x, y) de la cuadrícula. */
    private static double azar(int x, int y, long semilla) {
        long h = semilla * 0x9E3779B97F4A7C15L + x * 0xC2B2AE3D27D4EB4FL + y * 0x165667B19E3779F9L;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        return (h >>> 11) / (double) (1L << 53);
    }

    private static double suavizar(double t) {
        return t * t * (3 - 2 * t);
    }

    private static double limitar(double t) {
        return Math.max(0, Math.min(1, t));
    }
}
