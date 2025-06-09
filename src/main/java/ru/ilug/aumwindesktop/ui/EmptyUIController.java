package ru.ilug.aumwindesktop.ui;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.ui.component.SceneKind;

import java.util.List;

@Profile("test")
@Primary
@Component
public class EmptyUIController implements IUIController {
    @Override
    public void showScene(SceneKind kind) {

    }

    @Override
    public void setApplicationContext(ConfigurableApplicationContext context) {

    }

    @Override
    public void updateStatisticsTable(List<ApplicationStatistic> statistics) {

    }

    @Override
    public boolean isShowing() {
        return true;
    }
}
