package ru.ilug.aumwindesktop;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.ilug.aumwindesktop.service.ApplicationMonitorService;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Log4j2
@SpringBootApplication
@EnableScheduling
public class AumWinDesktopApplication extends Application {

    private static String[] args;
    private static CompletableFuture<ConfigurableApplicationContext> applicationContextFuture;

    private Stage primaryStage;
    private TrayIcon trayIcon;

    public static void main(String[] args) {
        AumWinDesktopApplication.args = args;
        Application.launch(AumWinDesktopApplication.class, args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        stage.setTitle("Application Usage Monitor");

        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            log.error("Failed to load icon", e);
        }

        Text text = new Text("Starting application...");
        text.setStyle("-fx-font-size: 20;");

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(text);

        Scene scene = new Scene(vBox, 800, 600);
        stage.setScene(scene);

        setupTrayIcon();

        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            minimizeToTray();
        });

        stage.show();

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

    private void setupTrayIcon() {
        SystemTray tray = SystemTray.getSystemTray();

        java.awt.Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon.png"));

        PopupMenu popup = createTrayPopupMenu(tray);

        trayIcon = new TrayIcon(image, "Application Usage Monitor", popup);
        trayIcon.setImageAutoSize(true);

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Platform.runLater(() -> showStage());
                }
            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Failed to add tray icon");
        }
    }

    private PopupMenu createTrayPopupMenu(SystemTray tray) {
        PopupMenu popup = new PopupMenu();

        MenuItem showItem = new MenuItem("Open");
        showItem.addActionListener(e -> Platform.runLater(this::showStage));

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            Platform.exit();
            tray.remove(trayIcon);
            applicationContextFuture.thenAcceptAsync(SpringApplication::exit);
        });

        popup.add(showItem);
        popup.addSeparator();
        popup.add(exitItem);
        return popup;
    }

    private void minimizeToTray() {
        Platform.runLater(() -> {
            primaryStage.hide();
            Platform.setImplicitExit(false);
        });
    }

    private void showStage() {
        if (primaryStage != null) {
            primaryStage.show();
            primaryStage.toFront();

            applicationContextFuture.thenAccept(context ->
                    context.getBean(ApplicationMonitorService.class).updateStatistics());
        }
    }
}
