package ru.ilug.aumwindesktop.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.springframework.context.ApplicationContext;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.data.model.User;
import ru.ilug.aumwindesktop.service.AuthorizationFlowService;
import ru.ilug.aumwindesktop.util.TimeUtil;

import java.util.List;

public class MainScene extends Scene {

    private final ApplicationContext context;

    private final VBox rootBox;
    private final VBox header;
    private final TableView<ApplicationStatistic> tableView;

    public MainScene(ApplicationContext context) {
        super(new VBox(), 800, 600);

        this.context = context;

        rootBox = (VBox) getRoot();

        header = createHeader();
        tableView = createStatisticTable();

        rootBox.getChildren().addAll(header, tableView);
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.setPadding(new Insets(10));
        header.prefWidthProperty().bind(rootBox.widthProperty());
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setStyle("-fx-background-color: -color-base-1;");

        return header;
    }

    private Button createLoginButton() {
        Button button = new Button("Login via GitHub");
        button.setDefaultButton(true);
        button.setAlignment(Pos.CENTER_RIGHT);
        button.setOnMouseClicked(event -> {
            AuthorizationFlowService authFlow = context.getBean(AuthorizationFlowService.class);
            authFlow.start();

            button.setDisable(true);
        });

        return button;
    }

    private HBox createAvatar(User user) {
        Text name = new Text(user.getName());

        ImageView imageView = new ImageView(user.getAvatarUrl());
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);

        Circle clip = new Circle();
        clip.setRadius(Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2);
        clip.setCenterX(imageView.getFitWidth() / 2);
        clip.setCenterY(imageView.getFitHeight() / 2);

        imageView.setClip(clip);

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setSpacing(8);
        hbox.getChildren().addAll(name, imageView);

        return hbox;
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

    public void updateHeaderState(boolean authorized, User user) {
        header.getChildren().clear();

        if (authorized) {
            header.getChildren().add(createAvatar(user));
        } else {
            header.getChildren().add(createLoginButton());
        }
    }

    public void updateStatisticsTable(List<ApplicationStatistic> statistics) {
        ObservableList<ApplicationStatistic> list = FXCollections.observableArrayList(statistics);
        Platform.runLater(() -> {
            tableView.getItems().setAll(list);
            tableView.sort();
        });
    }
}
