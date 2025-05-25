package ru.ilug.aumwindesktop.service;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ilug.aumwindesktop.AumWinDesktopApplication;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.web.ServiceWebApi;

import java.util.concurrent.TimeUnit;

@Service
public class UIService {

    private final ServiceWebApi serviceWebApi;

    private final Stage primaryStage;
    private final TableView<ApplicationStatistic> tableView;

    public UIService(ServiceWebApi serviceWebApi) {
        this.serviceWebApi = serviceWebApi;

        primaryStage = AumWinDesktopApplication.getPrimaryStage();

        FlowPane root = new FlowPane();

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Важно для растягивания колонок
        tableView.prefHeightProperty().bind(root.heightProperty());
        tableView.prefWidthProperty().bind(root.widthProperty());

        TableColumn<ApplicationStatistic, String> exePathColumn = new TableColumn<>("Exe Path");
        exePathColumn.setCellValueFactory(new PropertyValueFactory<>("exePath"));
        tableView.getColumns().add(exePathColumn);

        TableColumn<ApplicationStatistic, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        tableView.getColumns().add(timeColumn);

        root.getChildren().add(tableView);

        Platform.runLater(() -> AumWinDesktopApplication.getScene().setRoot(root));
    }

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.SECONDS)
    public void getStatistics() {
        serviceWebApi.getStatistics()
                .collectList()
                .subscribe(statistics -> {
                    ObservableList<ApplicationStatistic> list = FXCollections.observableArrayList(statistics);
                    Platform.runLater(() -> tableView.setItems(list));
                });
    }
}
