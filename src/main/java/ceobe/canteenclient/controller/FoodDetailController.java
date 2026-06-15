package ceobe.canteenclient.controller;


import ceobe.canteenclient.entity.FoodItem;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.function.Consumer;

public class FoodDetailController {

    @FXML private ImageView foodImage;
    @FXML private Label imagePlaceholder;
    @FXML private Label nameLabel;
    @FXML private Label locationLabel;
    @FXML private Label priceLabel;
    @FXML private Label scoreLabel;
    @FXML private Label tagLabel;
    @FXML private TextArea introArea;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setFoodItem(FoodItem item) {
        nameLabel.setText(item.getName());
        locationLabel.setText("地点：" + item.getLocation());
        //priceLabel.setText(String.format("价格：￥%.1f", item.getPrice()));
        priceLabel.setText(String.format("价格：￥%d", item.getPrice()));
        scoreLabel.setText(String.format("评分：%.1f / 5.0", item.getScore()));
        tagLabel.setText("标签：" + item.getTagsText());
        introArea.setText(item.getDescription());

        /*if (item.getImagePath() != null && !item.getImagePath().isBlank()) {
            try {
                Image image = new Image(getClass().getResourceAsStream(item.getImagePath()));
                foodImage.setImage(image);
                imagePlaceholder.setVisible(false);
            } catch (Exception ex) {
                foodImage.setImage(null);
                imagePlaceholder.setVisible(true);
            }
        } else {*/
            foodImage.setImage(null);
            imagePlaceholder.setVisible(true);
        //}
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            //mainController.showFoodPanel();
            mainController.switchPanel(null, null, false);
        }
    }
}