package ru.ilug.aumwindesktop.ui.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import org.springframework.context.ApplicationContext;
import ru.ilug.aumwindesktop.event.ui.UserUpdateEvent;
import ru.ilug.aumwindesktop.service.UserService;
import ru.ilug.aumwindesktop.ui.IUIController;
import ru.ilug.aumwindesktop.ui.component.SceneKind;
import ru.ilug.aumwindesktop.ui.component.UserComponent;
import ru.ilug.aumwindesktop.util.ComponentsUtil;

public class SettingsScene extends Scene {

    public SettingsScene(ApplicationContext context) {
        super(new VBox(), 800, 600);

        VBox root = (VBox) getRoot();
        root.setAlignment(Pos.TOP_CENTER);

        VBox contentBox = new VBox();
        contentBox.setPrefWidth(400);
        contentBox.setMaxWidth(400);
        contentBox.setMinWidth(250);
        contentBox.setPadding(new Insets(8, 0, 8, 0));
        contentBox.setSpacing(8);
        root.getChildren().add(contentBox);

        Button backButton = ComponentsUtil.createButtonWithIcon("Back", "/img/arrow_back.png");
        backButton.setAlignment(Pos.CENTER_LEFT);
        backButton.setGraphicTextGap(-4);
        backButton.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                context.getBean(IUIController.class).showScene(SceneKind.MAIN);
            }
        });

        UserComponent userComponent = new UserComponent(context);
        userComponent.setAlignment(Pos.CENTER_LEFT);
        AnchorPane.setLeftAnchor(userComponent, 8d);
        AnchorPane.setTopAnchor(userComponent, 8d);
        AnchorPane.setBottomAnchor(userComponent, 8d);

        Button logoutButton = new Button("Log out");
        logoutButton.getStyleClass().add("danger");
        logoutButton.setAlignment(Pos.CENTER_RIGHT);
        logoutButton.setVisible(false);
        AnchorPane.setRightAnchor(logoutButton, 8d);
        AnchorPane.setTopAnchor(logoutButton, 8d);
        AnchorPane.setBottomAnchor(logoutButton, 8d);
        logoutButton.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                context.getBean(UserService.class).logout();
            }
        });


        AnchorPane userBox = new AnchorPane();
        userBox.setStyle("-fx-background-color: -color-base-1; -fx-background-radius: 4;");
        userBox.setPrefWidth(400);

        userBox.getChildren().addAll(userComponent, logoutButton);

        contentBox.getChildren().addAll(backButton, userBox);

        addEventHandler(UserUpdateEvent.EVENT_TYPE, event -> logoutButton.setVisible(event.getUser() != null));
    }

}
