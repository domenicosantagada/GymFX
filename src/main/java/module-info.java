module uni.palestra {
    requires javafx.controls;
    requires javafx.fxml;


    opens uni.palestra to javafx.fxml;
    exports uni.palestra;
}