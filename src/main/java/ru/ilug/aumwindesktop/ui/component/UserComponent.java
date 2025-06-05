package ru.ilug.aumwindesktop.ui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.ilug.aumwindesktop.data.model.User;
import ru.ilug.aumwindesktop.event.ui.UserUpdateEvent;
import ru.ilug.aumwindesktop.service.AuthorizationFlowService;

@Slf4j
@Component
@Scope("prototype")
public class UserComponent extends HBox {

    private final ApplicationContext context;

    public UserComponent(ApplicationContext context) {
        this.context = context;

        setPrefHeight(32);
        setSpacing(8);
        setPadding(new Insets(4));

        addEventHandler(UserUpdateEvent.EVENT_TYPE, event -> refresh(event.getUser()));
    }

    public void refresh(User user) {
        if (user != null) {
            getChildren().setAll(createAvatar(user));
        } else {
            getChildren().setAll(createLoginButton());
        }
    }

    private Button createLoginButton() {
        Button button = new Button("Login via GitHub");
        button.setDefaultButton(true);
        button.setOnMouseClicked(event -> {
            AuthorizationFlowService authFlow = context.getBean(AuthorizationFlowService.class);
            authFlow.start();

            button.setDisable(true);
        });

        return button;
    }

    private Node[] createAvatar(User user) {
        Text name = new Text(user.getName());

        ImageView imageView = new ImageView(user.getAvatarUrl());
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);

        Circle clip = new Circle();
        clip.setRadius(Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2);
        clip.setCenterX(imageView.getFitWidth() / 2);
        clip.setCenterY(imageView.getFitHeight() / 2);

        imageView.setClip(clip);

        return new Node[]{imageView, name};
    }
}
