package uni.palestra;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class GymApp extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/uni/palestra/gym-view.fxml"));
        Scene scene = new Scene(loader.load());

        GymController controller = loader.getController();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (controller.isOverlayOpen()) {
                    controller.closeOverlay();
                } else {
                    stage.close();
                }
            }
        });

        stage.setTitle("Gym Exercise - Domenico");
        stage.setScene(scene);
        stage.show();
    }
}