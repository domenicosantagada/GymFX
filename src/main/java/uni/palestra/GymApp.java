package uni.palestra;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

public class GymApp extends Application {

    private FlowPane imageGrid;
    private StackPane rootPane; // StackPane per permettere l'overlay dell'immagine ingrandita
    private VBox categoryBox;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        rootPane = new StackPane();
        BorderPane mainLayout = new BorderPane();

        // --- SIDEBAR ---
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(240);
        sidebar.getStyleClass().add("sidebar");

        Label logo = new Label("GYM APP");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: 900; -fx-padding: 0 0 30 25;");

        categoryBox = new VBox();
        ScrollPane sideScroll = new ScrollPane(categoryBox);
        sideScroll.setFitToWidth(true);

        sidebar.getChildren().addAll(logo, sideScroll);

        // --- GRID AREA ---
        imageGrid = new FlowPane();
        imageGrid.getStyleClass().add("image-grid");
        imageGrid.setHgap(20);
        imageGrid.setVgap(20);
        imageGrid.setAlignment(Pos.TOP_LEFT);

        ScrollPane mainScroll = new ScrollPane(imageGrid);
        mainScroll.setFitToWidth(true);

        mainLayout.setLeft(sidebar);
        mainLayout.setCenter(mainScroll);

        rootPane.getChildren().add(mainLayout);

        Scene scene = new Scene(rootPane, 1280, 800);

        // Carica CSS
        URL css = getClass().getResource("/uni/palestra/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        // CARICAMENTO AUTOMATICO
        loadInternalResources();

        // Gestione tasto ESC globale
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                // Se c'è un'immagine aperta (overlay), la chiude. Altrimenti chiude l'app.
                if (rootPane.getChildren().size() > 1) {
                    rootPane.getChildren().remove(1);
                } else {
                    stage.close();
                }
            }
        });

        stage.setTitle("Gym Exercise - Domenico");
        stage.setScene(scene);
        stage.show();
    }

    private void loadInternalResources() {
        // Cerchiamo la cartella dentro resources/uni/palestra/ESERCIZI PALESTRA
        try {
            URL res = getClass().getResource("/uni/palestra/ESERCIZI PALESTRA");
            if (res == null) {
                System.out.println("Cartella immagini non trovata nelle risorse!");
                return;
            }

            File mainDir = new File(res.toURI());
            File[] categories = mainDir.listFiles(File::isDirectory);

            if (categories != null) {
                Arrays.sort(categories);
                for (File cat : categories) {
                    Button btn = new Button(cat.getName());
                    btn.getStyleClass().add("category-button");
                    btn.setMaxWidth(Double.MAX_VALUE);
                    btn.setOnAction(e -> {
                        // Reset stile bottoni
                        categoryBox.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
                        btn.getStyleClass().add("selected");
                        displayExercises(cat);
                    });
                    categoryBox.getChildren().add(btn);
                }
                // Carica la prima categoria di default
                if (categories.length > 0) ((Button) categoryBox.getChildren().get(0)).fire();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayExercises(File folder) {
        imageGrid.getChildren().clear();
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));

        if (files != null) {
            for (File f : files) {
                VBox card = createCard(f);
                imageGrid.getChildren().add(card);
            }
        }
    }

    private VBox createCard(File file) {
        VBox card = new VBox(10);
        card.getStyleClass().add("exercise-card");
        card.setAlignment(Pos.CENTER);

        // Anteprima piccola
        Image img = new Image(file.toURI().toString(), 180, 180, true, true, true);
        ImageView iv = new ImageView(img);

        Label title = new Label(file.getName().replace("-BG.png", "").replace("-", " "));
        title.getStyleClass().add("exercise-title");
        title.setWrapText(true);
        title.setPrefWidth(160);

        card.getChildren().addAll(iv, title);

        // Click per ingrandire
        card.setOnMouseClicked(e -> showFullImage(file));

        return card;
    }

    private void showFullImage(File file) {
        VBox overlay = new VBox();
        overlay.getStyleClass().add("overlay");
        overlay.setAlignment(Pos.CENTER);

        ImageView fullIv = new ImageView(new Image(file.toURI().toString()));
        fullIv.setPreserveRatio(true);
        fullIv.setFitHeight(700); // Altezza massima

        Label hint = new Label("Premi ESC per tornare indietro");
        hint.setStyle("-fx-text-fill: #888; -fx-padding: 20;");

        overlay.getChildren().addAll(fullIv, hint);

        // Rimuovi al click sull'oscurità
        overlay.setOnMouseClicked(e -> rootPane.getChildren().remove(overlay));

        rootPane.getChildren().add(overlay);
    }
}