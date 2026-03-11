package uni.palestra;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class GymController {

    @FXML
    private StackPane rootPane;
    @FXML
    private VBox categoryBox;
    @FXML
    private FlowPane imageGrid;

    // Riferimento al pannello centrale per scambiare la vista
    @FXML
    private ScrollPane mainScrollPane;

    private Set<File> selectedExercises = new HashSet<>();

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

                // --- PULSANTE: LA MIA SCHEDA ---
                Button btnScheda = new Button("LA MIA SCHEDA");
                btnScheda.setStyle("-fx-background-color: #00E676; -fx-text-fill: #121212; -fx-font-weight: 900; -fx-font-size: 14px; -fx-padding: 12px;");
                btnScheda.setMaxWidth(Double.MAX_VALUE);
                btnScheda.setOnAction(e -> {
                    categoryBox.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
                    btnScheda.getStyleClass().add("selected");
                    displayScheda(); // Mostra la pagina Scheda
                });
                categoryBox.getChildren().add(btnScheda);

                for (File cat : categories) {
                    Button btn = new Button(cat.getName());
                    btn.getStyleClass().add("category-button");
                    btn.setMaxWidth(Double.MAX_VALUE);
                    btn.setOnAction(e -> {
                        categoryBox.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
                        btn.getStyleClass().add("selected");
                        displayExercises(cat); // Mostra una categoria normale
                    });
                    categoryBox.getChildren().add(btn);
                }

                if (categoryBox.getChildren().size() > 1) {
                    ((Button) categoryBox.getChildren().get(1)).fire();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayExercises(File folder) {
        // Se veniamo dalla Scheda, ripristiniamo la vista a griglia normale nel pannello
        if (mainScrollPane.getContent() != imageGrid) {
            mainScrollPane.setContent(imageGrid);
        }

        imageGrid.getChildren().clear();
        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".PNG"));

        if (files != null) {
            for (File f : files) {
                imageGrid.getChildren().add(createCard(f));
            }
        }
    }

    private VBox createCard(File file) {
        VBox card = new VBox(10);
        card.getStyleClass().add("exercise-card");

        if (selectedExercises.contains(file)) {
            card.getStyleClass().add("highlighted-card");
        }

        card.setAlignment(Pos.CENTER);

        ImageView iv = new ImageView(new Image(file.toURI().toString(), 180, 180, true, true, true));
        Label title = new Label(file.getName().toLowerCase().replace("-bg.png", "").replace(".png", "").replace("-", " ").toUpperCase());

        title.getStyleClass().add("exercise-title");
        title.setWrapText(true);
        title.setPrefWidth(160);

        card.getChildren().addAll(iv, title);

        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                if (selectedExercises.contains(file)) {
                    selectedExercises.remove(file);
                    card.getStyleClass().remove("highlighted-card");
                } else {
                    selectedExercises.add(file);
                    card.getStyleClass().add("highlighted-card");
                }
            } else if (e.getButton() == MouseButton.PRIMARY) {
                showFullImage(file);
            }
        });

        return card;
    }

    // --- GENERAZIONE PAGINA "SCHEDA" ---
    private void displayScheda() {
        VBox schedaBox = new VBox(40);
        schedaBox.setPadding(new Insets(30));

        if (selectedExercises.isEmpty()) {
            mostraMessaggioVuoto(schedaBox);
        } else {
            // Raggruppa i file in base alla cartella (Bicipiti, Tricipiti, ecc.)
            Map<String, List<File>> grouped = selectedExercises.stream()
                    .collect(Collectors.groupingBy(f -> f.getParentFile().getName()));

            List<String> sortedCats = new ArrayList<>(grouped.keySet());
            Collections.sort(sortedCats);

            // Costruisce la pagina
            for (String cat : sortedCats) {
                Label title = new Label(cat);
                title.setStyle("-fx-text-fill: #00E676; -fx-font-size: 26px; -fx-font-weight: 900; -fx-border-color: #00E676; -fx-border-width: 0 0 2 0; -fx-padding: 0 0 5 0;");
                title.setMaxWidth(Double.MAX_VALUE);

                FlowPane categoryGrid = new FlowPane(20, 20);
                VBox section = new VBox(15, title, categoryGrid);

                for (File f : grouped.get(cat)) {
                    VBox miniCard = new VBox(10);
                    miniCard.setAlignment(Pos.CENTER);
                    miniCard.getStyleClass().add("exercise-card"); // Stesso look

                    ImageView iv = new ImageView(new Image(f.toURI().toString(), 180, 180, true, true, true));
                    Label name = new Label(f.getName().toLowerCase().replace("-bg.png", "").replace(".png", "").replace("-", " ").toUpperCase());
                    name.getStyleClass().add("exercise-title");
                    name.setWrapText(true);
                    name.setPrefWidth(160);

                    miniCard.getChildren().addAll(iv, name);

                    miniCard.setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            showFullImage(f); // Tasto Sinistro: Ingrandisci
                        } else if (e.getButton() == MouseButton.SECONDARY) {
                            // Tasto Destro: Rimuovi dalla scheda in tempo reale
                            selectedExercises.remove(f);
                            categoryGrid.getChildren().remove(miniCard);

                            if (categoryGrid.getChildren().isEmpty()) {
                                schedaBox.getChildren().remove(section); // Elimina anche il titolo se non ci sono più esercizi
                            }
                            if (selectedExercises.isEmpty()) {
                                mostraMessaggioVuoto(schedaBox);
                            }
                        }
                    });

                    categoryGrid.getChildren().add(miniCard);
                }
                schedaBox.getChildren().add(section);
            }
        }

        // --- LA MAGIA E' QUI ---
        // Sostituiamo il contenuto del pannello centrale con la nuova impaginazione
        mainScrollPane.setContent(schedaBox);
    }

    private void mostraMessaggioVuoto(VBox container) {
        container.getChildren().clear();
        Label emptyLbl = new Label("La tua scheda è vuota.\nVai nelle categorie e fai Click Destro sugli esercizi per aggiungerli qui.");
        emptyLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        container.getChildren().add(emptyLbl);
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

    public void closeOverlay() {
        if (rootPane.getChildren().size() > 1) {
            rootPane.getChildren().remove(1);
        }
    }

    public boolean isOverlayOpen() {
        return rootPane.getChildren().size() > 1;
    }
}