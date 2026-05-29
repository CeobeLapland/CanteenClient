package ceobe.canteenclient.controller;

import ceobe.canteenclient.entity.FoodItem;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentPane;

    private Stage stage;
    private double xOffset;
    private double yOffset;

    private Node foodRoot;
    private FoodController foodController;

    private Node detailRoot;
    private FoodDetailController detailController;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        //showFood();
    }

    //region 窗口拖动事件
    @FXML
    private void onTitleBarPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void onTitleBarDragged(MouseEvent event) {
        if (stage != null) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }

    @FXML
    private void minimizeWindow() {
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    @FXML
    private void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }
    //endregion

    @FXML
    public void showFoodPanel() {
        try {
            if (foodRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/FoodView.fxml"));
                foodRoot = loader.load();
                foodController = loader.getController();
                foodController.setMainController(this);
            }
            contentPane.getChildren().setAll(foodRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showChannelPanel() {
        showPlaceholder("频道功能开发中...");
    }

    @FXML
    private void showCookPanel() {
        showPlaceholder("菜谱功能开发中...");
    }

    @FXML
    private void showProfilePanel() {
        showPlaceholder("个人中心开发中...");
    }

    private void showPlaceholder(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #1451aa; -fx-font-weight: bold;");
        contentPane.getChildren().setAll(label);
    }

    public void showFoodDetail(FoodItem item) {
        try {
            if (detailRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/FoodDetail.fxml"));
                detailRoot = loader.load();
                detailController = loader.getController();
                detailController.setMainController(this);
            }
            detailController.setFoodItem(item);
            contentPane.getChildren().setAll(detailRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}