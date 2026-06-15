package ceobe.canteenclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class EditPersonalController {

    private MainController mainController;
    private String selectedAvatarPath;

    @FXML
    private TextField nameField;
    @FXML
    private TextField uidField;
    @FXML
    private TextField joinTimeField;
    @FXML
    private TextField permissionField;
    @FXML
    private ImageView avatarPreview;
    @FXML
    private Label avatarPickHint;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void fillForm(String name, String uid, String joinTime, String permission, String avatarPath) {
        nameField.setText(name);
        uidField.setText(uid);
        joinTimeField.setText(joinTime);
        permissionField.setText(permission);

        selectedAvatarPath = avatarPath;
        refreshAvatarPreview();
    }

    private void refreshAvatarPreview() {
        if (selectedAvatarPath != null && !selectedAvatarPath.isBlank()) {
            avatarPreview.setImage(new Image(new File(selectedAvatarPath).toURI().toString(), true));
            avatarPickHint.setVisible(false);
            avatarPickHint.setManaged(false);
        } else {
            avatarPreview.setImage(null);
            avatarPickHint.setVisible(true);
            avatarPickHint.setManaged(true);
        }
    }

    @FXML
    private void handleChooseAvatar(MouseEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择头像");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg")
        );

        Window window = avatarPreview.getScene().getWindow();
        File file = chooser.showOpenDialog(window);
        if (file != null) {
            selectedAvatarPath = file.getAbsolutePath();
            refreshAvatarPreview();
        }
    }

    @FXML
    private void handleConfirm() {
        if (mainController != null) {
            mainController.applyProfile(
                    nameField.getText().trim(),
                    uidField.getText().trim(),
                    joinTimeField.getText().trim(),
                    permissionField.getText().trim(),
                    selectedAvatarPath
            );
            mainController.backToPersonal();
        }
    }

    @FXML
    private void handleCancel() {
        if (mainController != null) {
            mainController.backToPersonal();
        }
    }
}