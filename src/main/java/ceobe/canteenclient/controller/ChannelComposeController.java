package ceobe.canteenclient.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.util.Arrays;
import java.util.Optional;

public class ChannelComposeController {

    @FXML private TextArea contentArea;
    @FXML private Label typeValueLabel;
    @FXML private Label foodValueLabel;

    private MainController mainController;

    private String selectedType = null;
    private String selectedFood = null;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setSelectedFood(String foodName) {
        this.selectedFood = foodName;
        foodValueLabel.setText(foodName == null || foodName.isBlank() ? "未选择" : foodName);
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showChannelPanel();
        }
    }

    @FXML
    private void handleChooseType() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "求助",
                FXCollections.observableArrayList(
                        Arrays.asList("求助", "二手", "活动", "吐槽", "经验", "表白", "其他")
                )
        );
        dialog.setTitle("选择帖子类型");
        dialog.setHeaderText("请选择一个帖子类型");
        dialog.setContentText("类型：");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(type -> {
            selectedType = type;
            typeValueLabel.setText(type);
        });
    }

    @FXML
    private void handleChooseFood() {
        if (mainController != null) {
            mainController.openFoodSelectorForPost(this);
        }
    }

    @FXML
    private void handlePublish() {
        String content = contentArea.getText() == null ? "" : contentArea.getText().trim();
        if (content.isEmpty()) {
            return;
        }

        // 这里就是你后面接数据库的入口
        if (mainController != null) {
            mainController.publishChannelDraft(content, selectedType, selectedFood);
        }

        contentArea.clear();
        selectedType = null;
        selectedFood = null;
        typeValueLabel.setText("未选择");
        foodValueLabel.setText("未选择");
    }
}