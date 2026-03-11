package uni.palestra;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

    // Memoria per gli esercizi evidenziati (la tua Scheda)
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

                // --- NUOVO PULSANTE IN CIMA: APRI LA MIA SCHEDA ---
                Button btnScheda = new Button("LA MIA SCHEDA");
                // Stile verde per farlo risaltare dagli altri
                btnScheda.setStyle("-fx-background-color: #00E676; -fx-text-fill: #121212; -fx-font-weight: 900; -fx-font-size: 14px; -fx-padding: 12px;");
                btnScheda.setMaxWidth(Double.MAX_VALUE);
                btnScheda.setOnAction(e -> openSchedaWindow()); // Apre la pagina della scheda
                categoryBox.getChildren().add(btnScheda);
                // --------------------------------------------------

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

                // Clicca in automatico sulla prima categoria muscolare per riempire il centro
                if (categoryBox.getChildren().size() > 1) {
                    ((Button) categoryBox.getChildren().get(1)).fire();
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

    private VBox createCard(File file) {
        VBox card = new VBox(10);
        card.getStyleClass().add("exercise-card");

        // Applica bordo verde se era già stato selezionato
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

        // Sinistro: Ingrandisci. Destro: Aggiungi/Rimuovi dalla scheda
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

    // --- NUOVA FUNZIONE: Costruisce la pagina con le categorie separate ---
    // --- FUNZIONE AGGIORNATA: Costruisce la pagina con eliminazione via Tasto Destro ---
    private void openSchedaWindow() {
        Stage schedaStage = new Stage();
        schedaStage.setTitle("La Mia Scheda di Allenamento");

        // Contenitore principale verticale
        VBox mainBox = new VBox(40);
        mainBox.setPadding(new Insets(30));
        mainBox.setStyle("-fx-background-color: #121212;"); // Sfondo scuro

        if (selectedExercises.isEmpty()) {
            mostraMessaggioVuoto(mainBox);
        } else {
            // Raggruppa i file in base al nome della cartella genitore
            Map<String, List<File>> grouped = selectedExercises.stream()
                    .collect(Collectors.groupingBy(f -> f.getParentFile().getName()));

            // Ordina i nomi delle categorie alfabeticamente
            List<String> sortedCats = new ArrayList<>(grouped.keySet());
            Collections.sort(sortedCats);

            // Per ogni categoria...
            for (String cat : sortedCats) {
                Label title = new Label(cat);
                title.setStyle("-fx-text-fill: #00E676; -fx-font-size: 26px; -fx-font-weight: 900; -fx-border-color: #00E676; -fx-border-width: 0 0 2 0; -fx-padding: 0 0 5 0;");
                title.setMaxWidth(Double.MAX_VALUE);

                FlowPane categoryGrid = new FlowPane(20, 20);
                VBox section = new VBox(15, title, categoryGrid);

                for (File f : grouped.get(cat)) {
                    VBox miniCard = new VBox(10);
                    miniCard.setAlignment(Pos.CENTER);

                    ImageView iv = new ImageView(new Image(f.toURI().toString(), 150, 150, true, true, true));
                    Label name = new Label(f.getName().toLowerCase().replace("-bg.png", "").replace(".png", "").replace("-", " ").toUpperCase());
                    name.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                    name.setWrapText(true);
                    name.setPrefWidth(140);
                    name.setAlignment(Pos.CENTER);

                    miniCard.getChildren().addAll(iv, name);

                    // GESTIONE DEL CLICK NELLA SCHEDA (Sinistro ingrandisce, Destro elimina)
                    miniCard.setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            showFullImage(f); // Clic sinistro: apri immagine
                        } else if (e.getButton() == MouseButton.SECONDARY) {
                            // Clic destro: Rimuovi l'esercizio!

                            // 1. Lo rimuove dalla memoria
                            selectedExercises.remove(f);

                            // 2. Lo fa sparire istantaneamente dalla finestra della scheda
                            categoryGrid.getChildren().remove(miniCard);

                            // 3. Se era l'ultimo esercizio di quella categoria (es. l'ultimo esercizio per i Bicipiti), nascondiamo anche il titolo "BICIPITI"
                            if (categoryGrid.getChildren().isEmpty()) {
                                mainBox.getChildren().remove(section);
                            }

                            // 4. Se elimini tutti gli esercizi e la scheda rimane completamente vuota, mostra il testo informativo
                            if (selectedExercises.isEmpty()) {
                                mostraMessaggioVuoto(mainBox);
                            }
                        }
                    });

                    categoryGrid.getChildren().add(miniCard);
                }

                mainBox.getChildren().add(section);
            }
        }

        ScrollPane scroll = new ScrollPane(mainBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #121212; -fx-background-color: transparent;");

        Scene scene = new Scene(scroll, 900, 700);
        schedaStage.setScene(scene);
        schedaStage.show();
    }

    // Metodo di supporto per mostrare il testo quando la scheda è vuota
    private void mostraMessaggioVuoto(VBox mainBox) {
        mainBox.getChildren().clear();
        Label emptyLbl = new Label("La tua scheda è vuota.\nVai nelle categorie e fai Click Destro sugli esercizi per aggiungerli qui.");
        emptyLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        mainBox.getChildren().add(emptyLbl);
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