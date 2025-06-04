package ru.ilug.aumwindesktop.ui.scene;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.ui.component.UserComponent;
import ru.ilug.aumwindesktop.util.TimeUtil;

import java.util.List;

@Slf4j
public class MainScene extends Scene {

    private final ApplicationContext context;

    private final VBox rootBox;
    private final TableView<ApplicationStatistic> tableView;

    public MainScene(ApplicationContext context) {
        super(new VBox(), 800, 600);

        this.context = context;

        rootBox = (VBox) getRoot();

        VBox header = createHeader();
        tableView = createStatisticTable();

        rootBox.getChildren().addAll(header, tableView);
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.setPadding(new Insets(10));
        header.prefWidthProperty().bind(rootBox.widthProperty());
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setStyle("-fx-background-color: -color-base-1;");

        UserComponent userComponent = context.getBean(UserComponent.class);
        userComponent.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().addAll(userComponent);

        return header;
    }

    @SuppressWarnings("unchecked")
    private TableView<ApplicationStatistic> createStatisticTable() {
        TableView<ApplicationStatistic> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.prefWidthProperty().bind(rootBox.widthProperty());

        TableColumn<ApplicationStatistic, String> pathColumn = new TableColumn<>("Application");
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("exePath"));
        pathColumn.setReorderable(false);

        TableColumn<ApplicationStatistic, Long> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("seconds"));
        timeColumn.setReorderable(false);
        timeColumn.setSortType(TableColumn.SortType.DESCENDING);
        timeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(TimeUtil.formatSeconds(value));
                }
            }
        });

        tableView.getSortOrder().add(timeColumn);
        tableView.getColumns().addAll(pathColumn, timeColumn);

        VBox.setVgrow(tableView, Priority.ALWAYS);

        return tableView;
    }

    public void updateStatisticsTable(List<ApplicationStatistic> statistics) {
        ObservableList<ApplicationStatistic> list = FXCollections.observableArrayList(statistics);
        Platform.runLater(() -> {
            tableView.getItems().setAll(list);
            tableView.sort();
        });
    }
}
