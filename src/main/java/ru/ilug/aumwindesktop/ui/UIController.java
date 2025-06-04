package ru.ilug.aumwindesktop.ui;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.event.AuthStatusUpdateEvent;

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

    @EventListener
    public void onAuthStatusUpdate(AuthStatusUpdateEvent event) {
        Platform.runLater(() -> mainScene.updateHeaderState(event.isAuthorized(), event.getUser()));
    }

    public void updateStatisticsTable(List<ApplicationStatistic> statistics) {
        mainScene.updateStatisticsTable(statistics);
    }

    public boolean isShowing() {
        return stage.isShowing();
    }
}
