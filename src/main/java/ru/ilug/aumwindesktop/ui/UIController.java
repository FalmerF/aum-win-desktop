package ru.ilug.aumwindesktop.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.event.application.AuthStatusUpdateEvent;
import ru.ilug.aumwindesktop.event.ui.UserUpdateEvent;
import ru.ilug.aumwindesktop.ui.scene.LoadingScene;
import ru.ilug.aumwindesktop.ui.scene.MainScene;

import java.util.List;
import java.util.Objects;

@Slf4j
public class UIController {

    private final Stage stage;

    private MainScene mainScene;

    public UIController(Stage stage) {
        this.stage = stage;
        setupStage();

        LoadingScene loadingScene = new LoadingScene();
        stage.setScene(loadingScene);

        stage.show();
    }

    private void setupStage() {
        stage.setTitle("Application Usage Monitor");

        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            log.error("Failed to load icon", e);
        }
    }

    public void setApplicationContext(ConfigurableApplicationContext context) {
        mainScene = new MainScene(context);

        Platform.runLater(() -> {
            stage.setScene(mainScene);
        });
    }

    public void updateStatisticsTable(List<ApplicationStatistic> statistics) {
        mainScene.updateStatisticsTable(statistics);
    }

    public boolean isShowing() {
        return stage.isShowing();
    }

    @EventListener
    public void onAuthStatusUpdateEvent(AuthStatusUpdateEvent event) {
        UserUpdateEvent userUpdateEvent = new UserUpdateEvent(event.getUser());
        Platform.runLater(() -> fireEvent(mainScene.getRoot(), userUpdateEvent));
    }

    private void fireEvent(Node node, UserUpdateEvent event) {
        node.fireEvent(event);

        if (node instanceof Parent parent) {
            for (Node childNode : parent.getChildrenUnmodifiable()) {
                fireEvent(childNode, event);
            }
        }
    }
}
