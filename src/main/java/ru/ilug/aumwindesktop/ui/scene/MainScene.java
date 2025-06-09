package ru.ilug.aumwindesktop.ui.scene;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.ui.IUIController;
import ru.ilug.aumwindesktop.ui.component.SceneKind;
import ru.ilug.aumwindesktop.ui.component.UserComponent;
import ru.ilug.aumwindesktop.util.ComponentsUtil;
import ru.ilug.aumwindesktop.util.TimeUtil;

import java.util.List;

@Slf4j
public class MainScene extends Scene {

    private final ApplicationContext context;

    private final HBox rootBox;
    private final TableView<ApplicationStatistic> tableView;

    public MainScene(ApplicationContext context) {
        super(new HBox(), 800, 600);

        this.context = context;

        rootBox = (HBox) getRoot();

        VBox sidePanel = createSidePanel();
        tableView = createStatisticTable();

        rootBox.getChildren().addAll(sidePanel, tableView);
    }

    private VBox createSidePanel() {
        VBox sidePanel = new VBox();
        sidePanel.prefHeightProperty().bind(rootBox.heightProperty());
        sidePanel.setMinWidth(100);
        sidePanel.setMaxWidth(200);
        sidePanel.setPrefWidth(200);
        sidePanel.setStyle("-fx-background-color: -color-base-1;");

        VBox navigationBox = new VBox();
        navigationBox.prefWidthProperty().bind(sidePanel.widthProperty());
        VBox.setVgrow(navigationBox, Priority.ALWAYS);

        Button tableNavButton = createNavigationButton(navigationBox, "Table", "/img/table.png");
        tableNavButton.setDisable(true);

        Button chartNavButton = createNavigationButton(navigationBox, "Chart", "/img/graph_1.png");

        navigationBox.getChildren().addAll(tableNavButton, chartNavButton);

        UserComponent userComponent = new UserComponent(context);
        userComponent.setAlignment(Pos.CENTER_LEFT);

        HBox userBox = new HBox(userComponent);
        userBox.setPadding(new Insets(8));

        Button settingsButton = ComponentsUtil.createButtonWithIcon("Settings", "/img/settings.png");
        settingsButton.prefWidthProperty().bind(sidePanel.widthProperty());
        settingsButton.setOnMouseClicked(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            context.getBean(IUIController.class).showScene(SceneKind.SETTINGS);
        });

        HBox settingsBox = new HBox(settingsButton);
        settingsBox.setPadding(new Insets(8));

        sidePanel.getChildren().addAll(userBox, navigationBox, settingsBox);

        return sidePanel;
    }

    private Button createNavigationButton(VBox navigationBox, String name, String icon) {
        Button navigationButton = ComponentsUtil.createButtonWithIcon(name, icon);
        navigationButton.prefWidthProperty().bind(navigationBox.widthProperty());
        navigationButton.getStyleClass().addAll("navigation-button");
        return navigationButton;
    }

    @SuppressWarnings("unchecked")
    private TableView<ApplicationStatistic> createStatisticTable() {
        TableView<ApplicationStatistic> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.prefHeightProperty().bind(rootBox.heightProperty());

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

        HBox.setHgrow(tableView, Priority.ALWAYS);

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
