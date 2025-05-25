package ru.ilug.aumwindesktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.CompletableFuture;

@Log4j2
@SpringBootApplication
@EnableScheduling
public class AumWinDesktopApplication extends Application {

    private static String[] args;
    private static CompletableFuture<ConfigurableApplicationContext> applicationContextFuture;

    @Getter
    private static Stage primaryStage;
    @Getter
    private static Scene scene;

    public static void main(String[] args) {
        AumWinDesktopApplication.args = args;
        Application.launch(AumWinDesktopApplication.class, args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Application Usage Monitor");

        scene = new Scene(new FlowPane(), 800, 600);
        primaryStage.setScene(scene);

        primaryStage.show();

        stage.setOnCloseRequest(event -> {
            applicationContextFuture.thenAcceptAsync(SpringApplication::exit);
        });

        applicationContextFuture = CompletableFuture.supplyAsync(() -> SpringApplication.run(AumWinDesktopApplication.class, args));
    }
}
