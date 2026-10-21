package GUISolucionador;

import GUISolucionador.styles.TexturaMadera;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

/**
 * Vista JavaFX de la GUI del solucionador, con el mismo estilo de puzzle de madera.
 * No sabe nada de JADE ni de las reglas del puzzle: solo coloca y desliza fichas.
 * Sus métodos deben llamarse en el hilo de JavaFX (Platform.runLater).
 */
public class Ventana {

    private static final int TAM = 96;    // lado de cada ficha
    private static final int ESPACIO = 6; // separación entre fichas
    private static final int MARCO = 22;  // grosor del marco de madera
    private static final int INTERIOR = 3 * TAM + 4 * ESPACIO;

    private static final Duration DURACION_MOVER = Duration.millis(170);
    private static final Duration DURACION_Mezclar = Duration.millis(380);
    private static final Duration PASO_SOLUCION = Duration.millis(250); // ritmo visible de la solución

    private static final int[] ORDENADO = {1, 2, 3, 4, 5, 6, 7, 8, 0};
    private static final String FUENTE = elegirFuente();

    private final Stage stage = new Stage();
    private final StackPane[] fichas = new StackPane[9]; // fichas[n] = ficha con el número n (1..8)
    private final int[] posicion = new int[9];           // posicion[n] = casilla donde está la ficha n
    private final Label mensaje = new Label();
    private final ProgressIndicator anillo = new ProgressIndicator();
    private Animation animacion;                         // animación en curso (o null)
    private Runnable alTerminar;                         // deja las fichas en su lugar final
    private boolean primerTablero = true;
    private Timeline solucion;                           // solución reproduciéndose (o null)

    public Ventana(String nombreAgente, Runnable onCerrar) {
        Pane piezas = new Pane();
        piezas.setPrefSize(INTERIOR, INTERIOR);
        piezas.setMaxSize(INTERIOR, INTERIOR);

        Random azar = new Random(8);
        for (int n = 1; n <= 8; n++) {
            fichas[n] = crearFicha(n, azar);
            piezas.getChildren().add(fichas[n]);
        }
        colocar(ORDENADO);

        anillo.getStyleClass().add("anillo");
        anillo.setPrefSize(18, 18);
        anillo.managedProperty().bind(anillo.visibleProperty());
        mensaje.getStyleClass().add("mensaje");
        HBox estado = new HBox(8, anillo, mensaje);
        estado.setAlignment(Pos.CENTER);
        estado.setMinHeight(22);

        VBox raiz = new VBox(22, crearTitulo(nombreAgente), crearTablero(piezas), estado);
        raiz.setAlignment(Pos.CENTER);
        raiz.getStyleClass().add("mesa");

        Scene escena = new Scene(raiz, Color.web("#1c1008"));
        escena.getStylesheets().add(Ventana.class.getResource("styles/madera.css").toExternalForm());

        stage.setTitle("8-Puzzle · " + nombreAgente);
        stage.setScene(escena);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> onCerrar.run());
    }

    public void mostrar() {
        stage.show();
    }

    public void cerrar() {
        stage.close();
    }

    /** Texto bajo el tablero; con 'esperando' aparece el anillo girando junto a él. */
    public void mostrarMensaje(String texto, boolean esperando) {
        mensaje.setText(texto);
        anillo.setVisible(esperando);
    }

    /**
     * Coloca las fichas según el tablero (arreglo de 9 enteros por filas, 0 = espacio vacío),
     * deslizándolas a su nuevo lugar. Cancela la solución que se esté reproduciendo.
     */
    public void mostrarTablero(int[] tablero) {
        cancelarSolucion();
        colocar(tablero);
    }

    /**
     * Reproduce una solución: vuelve al tablero inicial y aplica cada movimiento {desde, hacia}
     * a ritmo visible. Al terminar ejecuta 'alFinal'.
     */
    public void reproducirSolucion(int[] inicial, List<int[]> movimientos, Runnable alFinal) {
        cancelarSolucion();
        colocar(inicial);
        solucion = new Timeline();
        Duration t = DURACION_Mezclar.add(PASO_SOLUCION); // primero deja que se acomode el inicial
        for (int[] m : movimientos) {
            solucion.getKeyFrames().add(new KeyFrame(t, e -> animarMovimiento(m[0], m[1])));
            t = t.add(PASO_SOLUCION);
        }
        solucion.getKeyFrames().add(new KeyFrame(t, e -> {
            solucion = null;
            alFinal.run();
        }));
        solucion.play();
    }

    private void cancelarSolucion() {
        if (solucion != null) {
            solucion.stop();
            solucion = null;
        }
    }

    private void colocar(int[] tablero) {
        detenerAnimacion();
        ParallelTransition Mezclar = new ParallelTransition();
        for (int casilla = 0; casilla < tablero.length; casilla++) {
            int n = tablero[casilla];
            if (n == 0) {
                continue;
            }
            StackPane ficha = fichas[n];
            double antesX = ficha.getLayoutX() + ficha.getTranslateX();
            double antesY = ficha.getLayoutY() + ficha.getTranslateY();
            posicion[n] = casilla;
            ficha.relocate(x(casilla), y(casilla));
            ficha.setTranslateX(0);
            ficha.setTranslateY(0);
            if (!primerTablero) {
                // la ficha sigue visualmente donde estaba y se desliza a su lugar nuevo
                ficha.setTranslateX(antesX - x(casilla));
                ficha.setTranslateY(antesY - y(casilla));
                TranslateTransition t = new TranslateTransition(DURACION_Mezclar, ficha);
                t.setToX(0);
                t.setToY(0);
                t.setInterpolator(Interpolator.EASE_BOTH);
                Mezclar.getChildren().add(t);
            }
        }
        primerTablero = false;
        reproducir(Mezclar, () -> {
            for (int n = 1; n <= 8; n++) {
                fichas[n].setTranslateX(0);
                fichas[n].setTranslateY(0);
            }
        });
    }

    /** Desliza la ficha que está en 'desde' hacia la casilla 'hacia' (el hueco). */
    private void animarMovimiento(int desde, int hacia) {
        StackPane ficha = fichaEn(desde);
        if (ficha == null) {
            return;
        }
        detenerAnimacion();
        TranslateTransition t = new TranslateTransition(DURACION_MOVER, ficha);
        t.setByX(x(hacia) - x(desde));
        t.setByY(y(hacia) - y(desde));
        t.setInterpolator(Interpolator.EASE_BOTH);
        posicion[numeroDe(ficha)] = hacia;
        reproducir(t, () -> {
            ficha.setTranslateX(0);
            ficha.setTranslateY(0);
            ficha.relocate(x(hacia), y(hacia));
        });
    }

    // ===================== Animaciones =====================

    private void reproducir(Animation a, Runnable fin) {
        animacion = a;
        alTerminar = fin;
        a.setOnFinished(e -> terminar());
        a.play();
    }

    /** Si hay una animación a medias, la corta y deja las fichas en su lugar final. */
    private void detenerAnimacion() {
        if (animacion != null && animacion.getStatus() == Animation.Status.RUNNING) {
            animacion.stop();
            terminar();
        }
    }

    private void terminar() {
        if (alTerminar != null) {
            alTerminar.run();
            alTerminar = null;
        }
    }

    // ===================== Construcción de la vista =====================

    private VBox crearTitulo(String nombreAgente) {
        Text titulo = new Text("8 · PUZZLE");
        titulo.setFont(Font.font(FUENTE, FontWeight.BOLD, 38));
        titulo.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#fbe9c6")), new Stop(1, Color.web("#d6a96a"))));
        titulo.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.7), 6, 0.3, 0, 2));

        Label subtitulo = new Label(nombreAgente);
        subtitulo.getStyleClass().add("subtitulo");

        VBox caja = new VBox(2, titulo, subtitulo);
        caja.setAlignment(Pos.CENTER);
        return caja;
    }

    /** Marco de nogal con una bandeja hundida donde se deslizan las fichas. */
    private StackPane crearTablero(Pane piezas) {
        double lado = INTERIOR + 2 * MARCO;

        Rectangle marco = new Rectangle(lado, lado);
        marco.setArcWidth(26);
        marco.setArcHeight(26);
        marco.setFill(new ImagePattern(TexturaMadera.NOGAL, 0, 0, TexturaMadera.TAM, TexturaMadera.TAM, false));
        marco.setStroke(Color.web("#24130a"));
        marco.setStrokeWidth(1.5);

        Rectangle biselMarco = bisel(lado, 26, 0.22, 0.45);

        Rectangle bandeja = new Rectangle(INTERIOR, INTERIOR);
        bandeja.setArcWidth(12);
        bandeja.setArcHeight(12);
        bandeja.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#24130a")), new Stop(1, Color.web("#3a2112"))));
        bandeja.setEffect(new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.9), 18, 0.2, 0, 4));

        StackPane tablero = new StackPane(marco, biselMarco, bandeja, piezas);
        tablero.setMaxSize(lado, lado);
        tablero.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.7), 28, 0.15, 0, 10));
        return tablero;
    }

    /** Una ficha de arce: textura propia, bisel, sombra y número grabado. */
    private StackPane crearFicha(int numero, Random azar) {
        // cada ficha toma un pedazo distinto de la textura para que ninguna sea igual
        double ox = azar.nextInt(TexturaMadera.TAM - TAM);
        double oy = azar.nextInt(TexturaMadera.TAM - TAM);

        Rectangle madera = new Rectangle(TAM, TAM);
        madera.setArcWidth(16);
        madera.setArcHeight(16);
        madera.setFill(new ImagePattern(TexturaMadera.ARCE, -ox, -oy, TexturaMadera.TAM, TexturaMadera.TAM, false));
        madera.setStroke(Color.web("#6b4423"));
        madera.setStrokeWidth(1);

        Rectangle biselFicha = bisel(TAM, 16, 0.38, 0.35);

        // línea interior, como el canto rebajado de una pieza tallada
        Rectangle canto = new Rectangle(TAM - 14, TAM - 14);
        canto.setArcWidth(10);
        canto.setArcHeight(10);
        canto.setFill(Color.TRANSPARENT);
        canto.setStroke(Color.rgb(90, 55, 25, 0.25));

        Text texto = new Text(String.valueOf(numero));
        texto.setFont(Font.font(FUENTE, FontWeight.BOLD, 44));
        texto.setFill(Color.web("#4a2a14"));
        // efecto grabado: sombra interior oscura + brillo claro debajo
        DropShadow brillo = new DropShadow(BlurType.GAUSSIAN, Color.rgb(255, 238, 205, 0.55), 1, 1, 0, 1.2);
        InnerShadow hundido = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.75), 3, 0.3, 0, 1.5);
        hundido.setInput(brillo);
        texto.setEffect(hundido);

        StackPane ficha = new StackPane(madera, biselFicha, canto, texto);
        ficha.setPrefSize(TAM, TAM);
        ficha.setUserData(numero);
        ficha.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.6), 7, 0.2, 2, 3));
        return ficha;
    }

    /** Capa de brillo arriba-izquierda y sombra abajo-derecha que simula el borde redondeado. */
    private static Rectangle bisel(double lado, double arco, double luz, double sombra) {
        Rectangle r = new Rectangle(lado, lado);
        r.setArcWidth(arco);
        r.setArcHeight(arco);
        r.setMouseTransparent(true);
        r.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.rgb(255, 240, 210, luz)),
                new Stop(0.18, Color.rgb(255, 240, 210, 0)),
                new Stop(0.80, Color.rgb(30, 15, 5, 0)),
                new Stop(1.00, Color.rgb(30, 15, 5, sombra))));
        return r;
    }

    /** Fuente con serifa disponible en el sistema (Linux, Windows o macOS). */
    private static String elegirFuente() {
        String[] preferidas = {"Noto Serif", "Cambria", "DejaVu Serif", "Book Antiqua", "Times New Roman"};
        for (String f : preferidas) {
            if (Font.getFamilies().contains(f)) {
                return f;
            }
        }
        return "Serif";
    }

    // ===================== Utilidades =====================

    private StackPane fichaEn(int casilla) {
        for (int n = 1; n <= 8; n++) {
            if (posicion[n] == casilla) {
                return fichas[n];
            }
        }
        return null;
    }

    private static int numeroDe(StackPane ficha) {
        return (Integer) ficha.getUserData();
    }

    private static double x(int casilla) {
        return ESPACIO + (casilla % 3) * (TAM + ESPACIO);
    }

    private static double y(int casilla) {
        return ESPACIO + (casilla / 3) * (TAM + ESPACIO);
    }
}
