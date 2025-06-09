package ru.ilug.aumwindesktop.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.event.application.AuthStatusUpdateEvent;
import ru.ilug.aumwindesktop.event.ui.UserUpdateEvent;
import ru.ilug.aumwindesktop.ui.component.SceneKind;
import ru.ilug.aumwindesktop.ui.scene.LoadingScene;
import ru.ilug.aumwindesktop.ui.scene.MainScene;
import ru.ilug.aumwindesktop.ui.scene.SettingsScene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class UIController implements IUIController {

    private final Stage stage;

    private final Map<SceneKind, Scene> sceneMap = new HashMap<>();

    private final String styles;

    public UIController(Stage stage) {
        this.stage = stage;
        setupStage();

        styles = loadStyles();

        registerScene(SceneKind.LOADING, new LoadingScene());
        showScene(SceneKind.LOADING);

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

    private String loadStyles() {
        return Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm();
    }

    private void registerScene(SceneKind kind, Scene scene) {
        scene.getStylesheets().add(styles);

        sceneMap.put(kind, scene);
    }

    public void showScene(SceneKind kind) {
        Scene scene = sceneMap.get(kind);

        if (scene == null) {
            throw new RuntimeException(String.format("Scene with kind %s not found", kind));
        }

        stage.setScene(scene);
    }

    public void setApplicationContext(ConfigurableApplicationContext context) {
        try {
            registerScene(SceneKind.MAIN, new MainScene(context));
            registerScene(SceneKind.SETTINGS, new SettingsScene(context));
            Platform.runLater(() -> showScene(SceneKind.MAIN));
        } catch (Exception e) {
            log.error("Error on init scenes", e);
        }
    }

    public void updateStatisticsTable(List<ApplicationStatistic> statistics) {
        ((MainScene) sceneMap.get(SceneKind.MAIN)).updateStatisticsTable(statistics);
    }

    public boolean isShowing() {
        return stage.isShowing();
    }

    @EventListener
    public void onAuthStatusUpdateEvent(AuthStatusUpdateEvent event) {
        UserUpdateEvent userUpdateEvent = new UserUpdateEvent(event.getUser());
        Platform.runLater(() -> {
            for (Scene scene : sceneMap.values()) {
                fireEvent(scene.getRoot(), userUpdateEvent);
            }
        });
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
