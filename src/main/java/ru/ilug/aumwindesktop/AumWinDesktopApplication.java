package ru.ilug.aumwindesktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableScheduling
public class AumWinDesktopApplication extends Application {

    private static String[] args;
    private static CompletableFuture<ApplicationContext> applicationContextFuture;

    public static void main(String[] args) {
        AumWinDesktopApplication.args = args;
        Application.launch(AumWinDesktopApplication.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Hello World!");
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(event -> System.out.println("Hello World!"));

        StackPane root = new StackPane();
        root.getChildren().add(btn);
        stage.setScene(new Scene(root, 800, 600));
        stage.show();

        stage.setOnCloseRequest(event -> {
            applicationContextFuture.thenAcceptAsync(SpringApplication::exit);
        });

        applicationContextFuture = CompletableFuture.supplyAsync(() -> SpringApplication.run(AumWinDesktopApplication.class, args));
    }
}
