package uni.palestra;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GymController {

    @FXML
    private StackPane rootPane;
    @FXML
    private VBox categoryBox;
    @FXML
    private FlowPane imageGrid;

    // Memoria per gli esercizi evidenziati
    private Set<File> selectedExercises = new HashSet<>();

    @FXML
    public void initialize() {
        loadInternalResources();
    }

    private VBox createCard(File file) {
        VBox card = new VBox(10);
        card.getStyleClass().add("exercise-card");

        // Se l'esercizio era già stato selezionato, riapplica il bordo quando cambi categoria
        if (selectedExercises.contains(file)) {
            card.getStyleClass().add("highlighted-card");
        }

        card.setAlignment(Pos.CENTER);

        ImageView iv = new ImageView(new Image(file.toURI().toString(), 180, 180, true, true, true));
        Label title = new Label(file.getName().toLowerCase().replace("-bg.png", "").replace("-", " ").toUpperCase());

        title.getStyleClass().add("exercise-title");
        title.setWrapText(true);
        title.setPrefWidth(160);

        card.getChildren().addAll(iv, title);

        // Modifica la gestione del click: Sinistro per ingrandire, Destro per evidenziare
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                // Click con il tasto destro: Seleziona / Deseleziona
                if (selectedExercises.contains(file)) {
                    selectedExercises.remove(file);
                    card.getStyleClass().remove("highlighted-card");
                } else {
                    selectedExercises.add(file);
                    card.getStyleClass().add("highlighted-card");
                }
            } else if (e.getButton() == MouseButton.PRIMARY) {
                // Click con il tasto sinistro: Mostra a schermo intero
                showFullImage(file);
            }
        });

        return card;
    }

    private void loadInternalResources() {
        try {
            URL res = getClass().getResource("/uni/palestra/ESERCIZI PALESTRA");
            if (res == null) return;

            File mainDir = new File(res.toURI());
            File[] categories = mainDir.listFiles(File::isDirectory);

            if (categories != null) {
                Arrays.sort(categories);
                for (File cat : categories) {
                    Button btn = new Button(cat.getName());
                    btn.getStyleClass().add("category-button");
                    btn.setMaxWidth(Double.MAX_VALUE);
                    btn.setOnAction(e -> {
                        categoryBox.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
                        btn.getStyleClass().add("selected");
                        displayExercises(cat);
                    });
                    categoryBox.getChildren().add(btn);
                }
                if (!categoryBox.getChildren().isEmpty()) {
                    ((Button) categoryBox.getChildren().get(0)).fire();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayExercises(File folder) {
        imageGrid.getChildren().clear();
        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".PNG"));

        if (files != null) {
            for (File f : files) {
                imageGrid.getChildren().add(createCard(f));
            }
        }
    }

    private void showFullImage(File file) {
        VBox overlay = new VBox(20);
        overlay.getStyleClass().add("overlay");
        overlay.setAlignment(Pos.CENTER);

        ImageView fullIv = new ImageView(new Image(file.toURI().toString()));
        fullIv.setPreserveRatio(true);
        fullIv.setFitHeight(700);

        Label hint = new Label("Clicca o premi ESC per chiudere");
        hint.setStyle("-fx-text-fill: white; -fx-opacity: 0.6;");

        overlay.getChildren().addAll(fullIv, hint);
        overlay.setOnMouseClicked(e -> rootPane.getChildren().remove(overlay));

        rootPane.getChildren().add(overlay);
    }

    // Metodo di utility per la gestione dell'ESC richiamato dalla App
    public void closeOverlay() {
        if (rootPane.getChildren().size() > 1) {
            rootPane.getChildren().remove(1);
        }
    }

    public boolean isOverlayOpen() {
        return rootPane.getChildren().size() > 1;
    }
}