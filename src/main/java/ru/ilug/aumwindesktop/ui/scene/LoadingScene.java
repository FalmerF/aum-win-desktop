package ru.ilug.aumwindesktop.ui.scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class LoadingScene extends Scene {

    public LoadingScene() {
        super(new VBox(), 800, 600);

        VBox root = (VBox) getRoot();
        root.setAlignment(Pos.CENTER);

        Text text = new Text("The application starts...");
        text.setStyle("-fx-font-size: 20;");

        root.getChildren().add(text);
    }

}
