package ru.ilug.aumwindesktop.util;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import ru.ilug.aumwindesktop.AumWinDesktopApplication;

import java.util.Objects;

public class ComponentsUtil {

    public static Button createButtonWithIcon(String text, String icon) {
        Image image = new Image(Objects.requireNonNull(AumWinDesktopApplication.class.getResourceAsStream(icon)));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);

        Rectangle overlay = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
        overlay.setStyle("-fx-fill: -color-base-9;");
        overlay.setClip(imageView);

        Button button = new Button(text);
        button.getStyleClass().addAll("flat");
        button.setPrefHeight(32);
        button.setGraphic(overlay);
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setAlignment(Pos.CENTER_LEFT);

        return button;
    }

}
