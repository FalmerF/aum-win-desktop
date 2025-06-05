package ru.ilug.aumwindesktop.ui.scene;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.ui.component.UserComponent;
import ru.ilug.aumwindesktop.util.TimeUtil;

import java.util.List;
import java.util.Objects;

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

        for (int i = 0; i < 5; i++) {
            Button navigationButton = createButtonWithIcon("Graph " + i, "/graph_1.png");
            navigationButton.prefWidthProperty().bind(navigationBox.widthProperty());
            navigationButton.getStyleClass().addAll("navigation-button");
            navigationBox.getChildren().addAll(navigationButton);

            if (i == 0) {
                navigationButton.setDisable(true);
            }
        }

        UserComponent userComponent = context.getBean(UserComponent.class);
        userComponent.setAlignment(Pos.CENTER_LEFT);

        HBox userBox = new HBox(userComponent);
        userBox.setPadding(new Insets(8));

        Button settingsButton = createButtonWithIcon("Settings", "/settings_white.png");
        settingsButton.prefWidthProperty().bind(sidePanel.widthProperty());

        HBox settingsBox = new HBox(settingsButton);
        settingsBox.setPadding(new Insets(8));

        sidePanel.getChildren().addAll(userBox, navigationBox, settingsBox);

        return sidePanel;
    }

    private Button createButtonWithIcon(String text, String icon) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(icon)));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);

        Rectangle overlay = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
        overlay.setStyle("-fx-fill: -color-base-9;");
        overlay.setClip(imageView);

        Button button = new Button(text);
        button.getStyleClass().addAll("flat");
        button.setPrefHeight(32);
        button.setGraphic(overlay);
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setAlignment(Pos.CENTER_LEFT);

        return button;
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
