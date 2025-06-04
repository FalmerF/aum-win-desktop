package ru.ilug.aumwindesktop.ui;

import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.ilug.aumwindesktop.service.ApplicationMonitorService;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class TrayController {

    private final Stage stage;
    private final CompletableFuture<ConfigurableApplicationContext> applicationContextFuture;

    private TrayIcon trayIcon;

    public void setup() {
        SystemTray tray = SystemTray.getSystemTray();

        java.awt.Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon.png"));

        PopupMenu popup = createPopupMenu(tray);

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

            stage.setOnCloseRequest(e -> {
                e.consume();
                minimizeToTray();
            });
        } catch (AWTException e) {
            System.err.println("Failed to add tray icon");
        }
    }

    private PopupMenu createPopupMenu(SystemTray tray) {
        PopupMenu popup = new PopupMenu();

        MenuItem showItem = new MenuItem("Open");
        showItem.addActionListener(e -> Platform.runLater(this::showStage));

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            Platform.exit();
            tray.remove(trayIcon);
            applicationContextFuture.thenAccept(SpringApplication::exit);
        });

        popup.add(showItem);
        popup.addSeparator();
        popup.add(exitItem);
        return popup;
    }

    private void minimizeToTray() {
        Platform.runLater(() -> {
            stage.hide();
            Platform.setImplicitExit(false);
        });
    }

    private void showStage() {
        if (stage != null) {
            stage.show();
            stage.toFront();
            applicationContextFuture.thenAccept(c -> c.getBean(ApplicationMonitorService.class).updateStatistics());
        }
    }
}
