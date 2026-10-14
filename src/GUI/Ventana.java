package GUI;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.IntConsumer;

/**
 * Vista JavaFX del Agente GUI. No sabe nada de JADE ni de las reglas del puzzle:
 * avisa por callbacks cuando se presiona "Comenzar", una casilla o se cierra la ventana.
 * Sus métodos deben llamarse en el hilo de JavaFX (Platform.runLater).
 */
public class Ventana {

    private static final int TAM = 80; // tamaño de cada casilla en px
    private static final Duration DURACION = Duration.millis(150);

    private static final String ESTILO_FICHA =
            "-fx-background-color: white; -fx-border-color: #555; -fx-font-size: 28px; -fx-font-weight: bold;";
    private static final String ESTILO_HUECO =
            "-fx-background-color: #ddd; -fx-border-color: #555;";

    private final Stage stage = new Stage();
    private final Label[] fichas = new Label[9];      // fichas[n] = ficha con el número n (1..8)
    private final int[] posicion = new int[9];        // posicion[n] = casilla donde está la ficha n
    private TranslateTransition animacion;           // animación en curso (o null)
    private boolean animando = false;

    public Ventana(Runnable onComenzar, IntConsumer onCasilla, Runnable onCerrar) {
        Pane tablero = new Pane();
        tablero.setPrefSize(TAM * 3, TAM * 3);
        tablero.setMaxSize(TAM * 3, TAM * 3);
        tablero.setStyle(ESTILO_HUECO); // el fondo gris es lo que se ve en el hueco

        for (int n = 1; n <= 8; n++) {
            Label ficha = new Label(String.valueOf(n));
            ficha.setPrefSize(TAM, TAM);
            ficha.setAlignment(Pos.CENTER);
            ficha.setStyle(ESTILO_FICHA);
            int numero = n;
            ficha.setOnMouseClicked(e -> {
                if (!animando) {
                    onCasilla.accept(posicion[numero]); // avisa qué casilla se tocó
                }
            });
            fichas[n] = ficha;
            tablero.getChildren().add(ficha);
        }

        Button comenzar = new Button("Comenzar");
        comenzar.setOnAction(e -> onComenzar.run());

        VBox raiz = new VBox(20, new Label("8-Puzzle"), tablero, comenzar);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(20));

        stage.setTitle("Agente GUI");
        stage.setScene(new Scene(raiz));
        stage.setOnCloseRequest(e -> onCerrar.run());
    }

    public void mostrar() {
        stage.show();
    }

    public void cerrar() {
        stage.close();
    }

    /** Coloca las fichas sin animación: arreglo de 9 enteros por filas, 0 = espacio vacío. */
    public void mostrarTablero(int[] tablero) {
        if (animacion != null) {
            animacion.stop(); // un tablero nuevo cancela la animación pendiente
            animando = false;
        }
        for (int casilla = 0; casilla < tablero.length; casilla++) {
            int n = tablero[casilla];
            if (n != 0) {
                posicion[n] = casilla;
                fichas[n].setTranslateX(0);
                fichas[n].setTranslateY(0);
                fichas[n].relocate(columna(casilla), fila(casilla));
            }
        }
    }

    /** Desliza la ficha que está en 'desde' hacia la casilla 'hacia' (el hueco). */
    public void animarMovimiento(int desde, int hacia) {
        Label ficha = fichaEn(desde);
        if (ficha == null) {
            return;
        }
        animando = true;
        TranslateTransition t = new TranslateTransition(DURACION, ficha);
        animacion = t;
        t.setByX(columna(hacia) - columna(desde));
        t.setByY(fila(hacia) - fila(desde));
        t.setOnFinished(e -> {
            ficha.setTranslateX(0);
            ficha.setTranslateY(0);
            ficha.relocate(columna(hacia), fila(hacia));
            animando = false;
        });
        posicion[Integer.parseInt(ficha.getText())] = hacia;
        t.play();
    }

    private Label fichaEn(int casilla) {
        for (int n = 1; n <= 8; n++) {
            if (posicion[n] == casilla) {
                return fichas[n];
            }
        }
        return null;
    }

    private static double columna(int casilla) {
        return (casilla % 3) * TAM;
    }

    private static double fila(int casilla) {
        return (casilla / 3) * TAM;
    }
}
