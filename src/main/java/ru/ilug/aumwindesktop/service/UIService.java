package ru.ilug.aumwindesktop.service;

import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.data.model.User;
import ru.ilug.aumwindesktop.event.AuthStatusUpdateEvent;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UIService {

    private final ApplicationContext context;
    private final Stage primaryStage;
    private final Scene scene;

    private VBox header;
    private TableView<ApplicationStatistic> tableView;

    @PostConstruct
    public void init() {
        VBox root = new VBox();

        header = createHeader(root);
        tableView = createStatisticTable(root);

        root.getChildren().addAll(header, tableView);

        Platform.runLater(() -> scene.setRoot(root));
    }

    private VBox createHeader(VBox root) {
        VBox header = new VBox();
        header.setPadding(new Insets(10));
        header.prefWidthProperty().bind(root.widthProperty());
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
    private TableView<ApplicationStatistic> createStatisticTable(VBox root) {
        TableView<ApplicationStatistic> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.prefWidthProperty().bind(root.widthProperty());

        tableView.getColumns().addAll(
                createStatisticTableColumn("exePath", "Exe Path"),
                createStatisticTableColumn("time", "Time")
        );

        VBox.setVgrow(tableView, Priority.ALWAYS);

        return tableView;
    }

    private TableColumn<ApplicationStatistic, String> createStatisticTableColumn(String name, String displayName) {
        TableColumn<ApplicationStatistic, String> column = new TableColumn<>(displayName);
        column.setCellValueFactory(new PropertyValueFactory<>(name));
        return column;
    }

    @EventListener
    public void onAuthStatusUpdate(AuthStatusUpdateEvent event) {
        Platform.runLater(() -> updateHeaderState(event.isAuthorized(), event.getUser()));
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
        Platform.runLater(() -> tableView.setItems(list));
    }

    public boolean isShowing() {
        return primaryStage.isShowing();
    }
}
