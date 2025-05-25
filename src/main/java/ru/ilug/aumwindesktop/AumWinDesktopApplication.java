package ru.ilug.aumwindesktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Log4j2
@SpringBootApplication
@EnableScheduling
public class AumWinDesktopApplication extends Application {

    private static String[] args;
    private static CompletableFuture<ConfigurableApplicationContext> applicationContextFuture;

    public static void main(String[] args) {
        AumWinDesktopApplication.args = args;
        Application.launch(AumWinDesktopApplication.class, args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Application Usage Monitor");

        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            log.error("Failed to load icon", e);
        }

        Scene scene = new Scene(new FlowPane(), 800, 600);
        stage.setScene(scene);

        stage.show();

        stage.setOnCloseRequest(event -> {
            applicationContextFuture.thenAcceptAsync(SpringApplication::exit);
        });

        applicationContextFuture = CompletableFuture.supplyAsync(() -> {
            return new SpringApplicationBuilder()
                    .sources(AumWinDesktopApplication.class)
                    .initializers(context -> {
                        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
                        beanFactory.registerSingleton("primaryStage", stage);
                        beanFactory.registerSingleton("scene", scene);
                    })
                    .run(args);
        });
    }
}
