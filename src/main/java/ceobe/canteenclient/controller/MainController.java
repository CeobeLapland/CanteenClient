package ceobe.canteenclient.controller;

import ceobe.canteenclient.entity.FoodItem;
import ceobe.canteenclient.entity.PostItem;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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


    private Node channelRoot;
    private ChannelController channelController;

    private Node channelDetailRoot;
    private ChannelDetailController channelDetailController;

    private Node channelComposeRoot;
    private ChannelComposeController channelComposeController;


    private Node eatWhatRoot;
    private EatWhatController eatWhatController;

    private Node cookFrameworkRoot;
    @FXML
    private StackPane cookContentPane;
    // cookFrameworkRoot 下辖两个子界面：菜谱列表和做饭界面
    private Node cookingRoot;
    private CookingController cookingController;

    private Node recipeListRoot;
    //private RecipeListController recipeListController;
    private Node recipeDetailRoot;
    //private RecipeDetailController recipeDetailController;


    private Node profileRoot;
    //private ProfileController profileController;



    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        showFoodPanel();
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
    public void showChannelPanel() {
        try {
            if (channelRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/ChannelView.fxml"));
                channelRoot = loader.load();
                channelController = loader.getController();
                channelController.setMainController(this);
            }
            contentPane.getChildren().setAll(channelRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showChannelDetailPanel(PostItem item) {
        try {
            if (channelDetailRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/ChannelDetail.fxml"));
                channelDetailRoot = loader.load();
                channelDetailController = loader.getController();
                channelDetailController.setMainController(this);
            }
            channelDetailController.setPostItem(item);
            contentPane.getChildren().setAll(channelDetailRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showChannelComposePanel() {
        try {
            if (channelComposeRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/ChannelCompose.fxml"));
                channelComposeRoot = loader.load();
                channelComposeController = loader.getController();
                channelComposeController.setMainController(this);
            }
            contentPane.getChildren().setAll(channelComposeRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里先作为“食物选择界面”的接入点。
     * 你后面把这个方法改成跳转你自己的食物选择页即可。
     */
    public void openFoodSelectorForPost(ChannelComposeController composeController) {
        showPlaceholder("食物选择界面（待接入）");
        // 你后面可以在这里接回传：
        // composeController.setSelectedFood("某个食物名称");
    }

    /**
     * 发布帖子时的接入点。你后面接数据库写入即可。
     */
    public void publishChannelDraft(String content, String type, String foodRef) {
        System.out.println("发布帖子：");
        System.out.println("内容 = " + content);
        System.out.println("类型 = " + type);
        System.out.println("关联食物 = " + foodRef);
    }

    /**
     * 详情页右侧“相似推荐”的接入点。
     * 你后面可根据类型、关键词、食物关联等规则返回推荐列表。
     */
    public List<PostItem> findSimilarChannelPosts(PostItem current) {
        return Collections.emptyList();
    }


    @FXML
    public void showEatWhatPanel()
    {
        try{
            if (eatWhatRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/EatWhatView.fxml"));
                eatWhatRoot = loader.load();
                eatWhatController = loader.getController();
                eatWhatController.setMainController(this);
            }
            contentPane.getChildren().setAll(eatWhatRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showCookFrameworkPanel() {
        try{
            if (cookFrameworkRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/CookingFramework.fxml"));
                // 这个界面的控制器只有两个按钮，所以直接用MainController来处理了，你后面可以把它拆成单独的CookFrameworkController
                loader.setController(this);

                cookFrameworkRoot = loader.load();

            }
            contentPane.getChildren().setAll(cookFrameworkRoot);

            //初始状态先显示做饭列表
            //showRecipeListPanel();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showProfilePanel() {
        showPlaceholder("个人中心开发中...");
    }

    @FXML
    public void showRecipeListPanel() {
        showPlaceholder("菜谱列表开发中...");
    }

    @FXML
    public void showCookingPanel() {
        try{
            if (cookingRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/CookingView.fxml"));
                cookingRoot = loader.load();
                cookingController = loader.getController();
                cookingController.setMainController(this);
            }
            // 这里是cookContentPane，因为做饭界面是嵌套在CookFramework里的
            cookContentPane.getChildren().setAll(cookingRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showPlaceholder(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #1451aa; -fx-font-weight: bold;");
        contentPane.getChildren().setAll(label);
    }


    public boolean isInSelectionMode = false;


    public void hadleFoodItemSelected(FoodItem item) {
        // 扩展
        if(!isInSelectionMode) {
            showFoodDetail(item);
        }
        else {
            // 这里是食物选择模式的回调，你后面可以在这里把选中的食物传回去
            System.out.println("选中了食物：" + item.getName());
        }
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