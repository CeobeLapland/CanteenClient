package ceobe.canteenclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class PersonalController {

    private MainController mainController;

    @FXML
    private Label nameLabel;
    @FXML
    private Label uidLabel;
    @FXML
    private Label joinTimeLabel;
    @FXML
    private Label permissionLabel;
    @FXML
    private Label postCountLabel;
    @FXML
    private Label viewCountLabel;
    @FXML
    private Label likeCountLabel;
    @FXML
    private Label commentCountLabel;
    @FXML
    private ImageView avatarImage;
    @FXML
    private Label avatarHint;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void refreshProfile(String name, String uid, String joinTime, String permission, String avatarPath) {
        nameLabel.setText(name);
        uidLabel.setText(uid);
        joinTimeLabel.setText(joinTime);
        permissionLabel.setText(permission);

        if (avatarPath != null && !avatarPath.isBlank()) {
            Image image = new Image(new File(avatarPath).toURI().toString(), true);
            avatarImage.setImage(image);
            avatarHint.setVisible(false);
            avatarHint.setManaged(false);
        } else {
            avatarImage.setImage(null);
            avatarHint.setVisible(true);
            avatarHint.setManaged(true);
        }
    }

    public void refreshStats(int postCount, int viewCount, int likeCount, int commentCount) {
        postCountLabel.setText(String.valueOf(postCount));
        viewCountLabel.setText(String.valueOf(viewCount));
        likeCountLabel.setText(String.valueOf(likeCount));
        commentCountLabel.setText(String.valueOf(commentCount));
    }

    @FXML
    private void handleModify() {
        mainController.showEditPersonalPanel();
    }

    @FXML
    private void handleMyPosts() {
        mainController.showMyPostsPanel();
    }

    @FXML
    private void handleHistory() {
        mainController.showHistoryPanel();
    }

    @FXML
    private void handleFavorites() {
        mainController.showFavoritesPanel();
    }

    @FXML
    private void handleSettings() {
        if (mainController != null) {
            mainController.handleSettings();
        }
    }

    @FXML
    private void handleAdmin() {
        if (mainController != null) {
            mainController.handleAdmin();
        }
    }
}
