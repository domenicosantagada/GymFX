package uni.palestra;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

public class GymController {

    @FXML
    private StackPane rootPane;
    @FXML
    private VBox categoryBox;
    @FXML
    private FlowPane imageGrid;

    @FXML
    public void initialize() {
        loadInternalResources();
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
                name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));

        if (files != null) {
            for (File f : files) {
                imageGrid.getChildren().add(createCard(f));
            }
        }
    }

    private VBox createCard(File file) {
        VBox card = new VBox(10);
        card.getStyleClass().add("exercise-card");
        card.setAlignment(Pos.CENTER);

        ImageView iv = new ImageView(new Image(file.toURI().toString(), 180, 180, true, true, true));
        Label title = new Label(file.getName().replace("-BG.png", "").replace("-", " "));
        title.getStyleClass().add("exercise-title");
        title.setWrapText(true);
        title.setPrefWidth(160);

        card.getChildren().addAll(iv, title);
        card.setOnMouseClicked(e -> showFullImage(file));
        return card;
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