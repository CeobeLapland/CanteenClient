package ceobe.canteenclient.controller;

import ceobe.canteenclient.entity.RecordCard;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.List;

public class ListController {

    private MainController mainController;

    @FXML
    private Label titleLabel;
    @FXML
    private TilePane cardPane;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setPanelTitle(String title) {
        titleLabel.setText(title);
    }

    public void setItems(List<RecordCard> items) {
        cardPane.getChildren().clear();

        for (RecordCard item : items) {
            VBox card = buildCard(item);
            cardPane.getChildren().add(card);
        }
    }

    private VBox buildCard(RecordCard item) {
        Label author = new Label("作者：" + item.getAuthor());
        Label title = new Label("标题：" + item.getTitle());
        Label views = new Label("浏览量：" + item.getViewCount());
        Label likes = new Label("点赞量：" + item.getLikeCount());
        Label time = new Label("浏览时间：" + item.getBrowseTime());

        author.setStyle("-fx-text-fill:#6b7a99;");
        title.setStyle("-fx-text-fill:#1f2d4d; -fx-font-size:16px; -fx-font-weight:bold;");
        views.setStyle("-fx-text-fill:#4a5f88;");
        likes.setStyle("-fx-text-fill:#4a5f88;");
        time.setStyle("-fx-text-fill:#4a5f88;");

        VBox card = new VBox(8, author, title, views, likes, time);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(16));
        card.setPrefWidth(450);
        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:18;" +
                        "-fx-border-radius:18;" +
                        "-fx-border-color:#dbe7ff;" +
                        "-fx-cursor:hand;"
        );

        card.setOnMouseClicked(e -> handleCardClick(item)); // 逻辑先置空
        return card;
    }

    private void handleCardClick(RecordCard item) {
        // 空函数占位
        System.out.println("点击了卡片：" + item.getTitle());
    }

    @FXML
    private void handleBack() {
        mainController.backToPersonal();
    }
}