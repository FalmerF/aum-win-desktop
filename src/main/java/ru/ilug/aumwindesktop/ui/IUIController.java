package ru.ilug.aumwindesktop.ui;

import org.springframework.context.ConfigurableApplicationContext;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.ui.component.SceneKind;

import java.util.List;

public interface IUIController {

    void showScene(SceneKind kind);

    void setApplicationContext(ConfigurableApplicationContext context);

    void updateStatisticsTable(List<ApplicationStatistic> statistics);

    boolean isShowing();

}
