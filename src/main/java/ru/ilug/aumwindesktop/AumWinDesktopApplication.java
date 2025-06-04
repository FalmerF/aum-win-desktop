package ru.ilug.aumwindesktop;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.ilug.aumwindesktop.service.UserService;
import ru.ilug.aumwindesktop.ui.TrayController;
import ru.ilug.aumwindesktop.ui.UIController;

import java.util.concurrent.CompletableFuture;

@Log4j2
@SpringBootApplication
@EnableScheduling
public class AumWinDesktopApplication extends Application {

    private static String[] args;
    private static UIController uiController;

    public static void main(String[] args) {
        AumWinDesktopApplication.args = args;
        Application.launch(AumWinDesktopApplication.class, args);
    }

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        uiController = new UIController(stage);

        CompletableFuture<ConfigurableApplicationContext> applicationContextFuture = CompletableFuture.supplyAsync(() -> {
            return new SpringApplicationBuilder()
                    .sources(AumWinDesktopApplication.class)
                    .initializers(context -> {
                        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
                        beanFactory.registerSingleton("uiController", uiController);
                    })
                    .run(args);
        });

        TrayController trayController = new TrayController(stage, applicationContextFuture);
        trayController.setup();

        applicationContextFuture.thenAccept(context -> {
            uiController.setApplicationContext(context);
            context.getBean(UserService.class).onApplicationLaunched();
        });
    }
}
