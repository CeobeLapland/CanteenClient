package ceobe.canteenclient.controller;

import ceobe.canteenclient.entity.PostItem;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChannelDetailController {

    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label timeLabel;
    @FXML private Label typeLabel;
    @FXML private TextArea contentArea;
    @FXML private Label viewLabel;
    @FXML private Label likeLabel;
    @FXML private Label commentCountLabel;
    @FXML private ListView<String> commentListView;
    @FXML private TextField commentField;
    @FXML private VBox recommendBox;

    private MainController mainController;
    private PostItem currentItem;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setPostItem(PostItem item) {
        this.currentItem = item;

        titleLabel.setText(item.getTitle());
        authorLabel.setText("发帖人：" + item.getAuthor());
        timeLabel.setText("时间：" + item.getShortTime());
        typeLabel.setText("类型：" + item.getType());
        contentArea.setText(item.getContent());

        refreshStats();
        loadComments();
        loadRecommendations();
    }

    private void refreshStats() {
        if (currentItem == null) return;
        viewLabel.setText("浏览量：" + currentItem.getViewCount());
        likeLabel.setText("点赞量：" + currentItem.getLikeCount());
        commentCountLabel.setText("评论数：" + currentItem.getCommentCount());
    }

    private void loadComments() {
        // 这里以后直接换成你的数据库评论数据
        commentListView.setItems(FXCollections.observableArrayList(
                "匿名用户：内容很实用。",
                "同学A：这个帖子我收藏了。"
        ));
    }

    private void loadRecommendations() {
        recommendBox.getChildren().clear();

        if (mainController == null || currentItem == null) {
            recommendBox.getChildren().add(new Label("暂无推荐"));
            return;
        }

        List<PostItem> rec = mainController.findSimilarChannelPosts(currentItem);
        if (rec == null || rec.isEmpty()) {
            Label empty = new Label("暂无推荐");
            empty.setStyle("-fx-text-fill: #6d7f98;");
            recommendBox.getChildren().add(empty);
            return;
        }

        for (PostItem item : rec) {
            recommendBox.getChildren().add(createRecommendCard(item));
        }
    }

    private Node createRecommendCard(PostItem item) {
        VBox box = new VBox(4);
        box.getStyleClass().add("food-card");
        box.setStyle("-fx-padding: 12; -fx-cursor: hand;");
        box.setOnMouseClicked(e -> {
            if (mainController != null) {
                mainController.showChannelDetailPanel(item);
            }
        });

        Label title = new Label(item.getTitle());
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 15px;");

        Label meta = new Label(item.getAuthor() + " · " + item.getShortTime());
        meta.getStyleClass().add("card-meta");

        box.getChildren().addAll(title, meta);
        return box;
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showChannelPanel();
        }
    }

    @FXML
    private void handleLike() {
        if (currentItem == null) return;
        currentItem.addLike();
        refreshStats();
    }

    @FXML
    private void handleSendComment() {
        if (currentItem == null) return;

        String text = commentField.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        commentListView.getItems().add(0, "我：" + text.trim());
        commentField.clear();

        currentItem.addComment();
        refreshStats();
    }
}