package ceobe.canteenclient.controller;

import ceobe.canteenclient.entity.FoodItem;
import ceobe.canteenclient.entity.PostItem;
import ceobe.canteenclient.entity.RecordCard;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class MainController {

    //region 组件声明
    @FXML
    private StackPane rootStackPane;

    @FXML
    private StackPane contentPane;


    //全局遮罩
    @FXML
    private AnchorPane toastPanel;
    @FXML
    private Label toastText;
    @FXML
    private Button closeToastButton;

    @FXML
    private Button foodPanelButton, channelPanelButton, eatWhatPanelButton, cookFrameworkButton, profilePanelButton;


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


    private Node personalRoot;
    private Node editRoot;
    private Node listRoot;

    private PersonalController personalController;
    private EditPersonalController editPersonalController;
    private ListController listController;


    private Node adminRoot;
    private AdminController adminController;

    //endregion


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        closeToast();
        //showFoodPanel();
        //因为要用页面栈，所以只能手动初始化按钮了
        foodPanelButton.setOnMouseClicked(event -> {
            switchPanel(new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    showFoodPanel();
                }
            }, null, true);
        });
        channelPanelButton.setOnMouseClicked(event -> {
            switchPanel(new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    showChannelPanel();
                }
            }, null, true);
        });
        eatWhatPanelButton.setOnMouseClicked(event -> {
            switchPanel(new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    showEatWhatPanel();
                }
            }, null, true);
        });
        cookFrameworkButton.setOnMouseClicked(event -> {
            switchPanel(new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    showCookFrameworkPanel();
                }
            }, null, true);
        });
        profilePanelButton.setOnMouseClicked(event -> {
            switchPanel(new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    showPersonalPanel();
                }
            }, null, true);
        });



        switchPanel(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                showFoodPanel();
            }
        }, null, true);
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

    //region 全局遮罩事件
    public void showToast(String message) {
        toastText.setText(message);
        toastPanel.setVisible(true);
    }

    @FXML
    private void closeToast() {
        toastPanel.setVisible(false);
    }

    private void showPlaceholder(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #1451aa; -fx-font-weight: bold;");
        contentPane.getChildren().setAll(label);
    }
    //endregion

    //准备写一个简单的页面栈，用命令模式，函数有带参和不带参两种，参数类型可以是基本数据类型，也可以是对象，甚至是函数式接口（回调）。
    //每次页面切换时，把当前页面的状态（包括页面标识和参数）压入栈中。返回上一页时，从栈顶弹出状态，根据状态信息恢复到对应页面和参数/或者直接调用前一步的命令
    private Stack<Consumer<Object>> panelStack = new Stack<>();
    //顺便把数据也存起来
    private Stack<Object> panelDataStack = new Stack<>();


    //给过两天的自己看，备忘录
    //现在虽然用了栈，但只是命令栈，并没有真正保存每个页面的状态（比如食物详情页是哪个食物），所以现在的回退只能回退到上一个页面，但参数会丢失。
    //并且切换界面的本质还是把StackPane的子节点替换掉，所以如果你想在回退时恢复到之前的状态，可能还需要在命令里保存一些参数，或者直接把整个页面的状态都保存下来（比如当前显示的是食物详情页，并且是哪个食物），这样回退时就能完全恢复了。


    /** 统一处理页面切换函数
     * 如果参数为null，则代表回退，不为null时，可以带参也可以不带
     * 话说java有委托delegate嘛
     */
    public void switchPanel(Consumer<Object> command, Object param, boolean isReplace) {
        System.out.println(panelStack.size()+" -> switchPanel: " + command + ", param: " + param + ", isReplace: " + isReplace);
        if(command == null) {
            //回退，即执行上一条
            panelStack.pop();//丢弃当前状态
            panelDataStack.pop();

            if(!panelStack.isEmpty()) {
                panelStack.peek().accept(null);//执行上一条状态
            }
            return;
        }

        //忘记加相同检验了，防止一个按钮点很多次导致重复页面被压入栈中
        //不过要怎么比较Consumer呢？只能比较它的toString了，反正每个lambda表达式的toString都是唯一的
        if(!panelStack.isEmpty() && panelStack.peek().toString().equals(command.toString())) {
            System.out.println("别一直点喵" + command);
            return;
        }

        if(isReplace) {
            //替换当前状态
            if(!panelStack.isEmpty()) {
                panelStack.pop();
                panelDataStack.pop();
            }
            panelStack.push(command);
            panelDataStack.push(param);

            command.accept(param);
        }
        else {
            //正常切换，压入新状态
            panelStack.push(command);
            panelDataStack.push(param);

            command.accept(param);
        }
        System.out.println("切换完当前页面栈大小: " + panelStack.size());
    }



    // region 食物面板相关
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



    public boolean isInSelectionMode = false;


    public void hadleFoodItemSelected(FoodItem item) {
        // 扩展
        if(!isInSelectionMode) {
            //showFoodDetail(item);
            switchPanel(new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    showFoodDetail(item);
                }
            }, null, false);
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
    // endregion



    // region 频道面板相关
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
        //showPlaceholder("食物选择界面（待接入）");
        showToast("还没做食物选择界面");
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
    // endregion



    // region 吃什么面板相关
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


    // endregion



    // region 做饭面板相关
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
    public void showRecipeListPanel() {
        //showPlaceholder("菜谱列表开发中...");
        showToast("还没做喵");
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

    // endregion



    // region 个人中心相关

    // 个人资料
    private String userName = "小刻";
    private String uid = "UID-10001";
    private String joinTime = "2025-01-01";
    private String permission = "普通用户";
    private String avatarPath = null;

    // 统计数据
    private int postCount = 12;
    private int viewCount = 3560;
    private int likeCount = 428;
    private int commentCount = 93;

    public void showPersonalPanel() {
        try {
            if (personalRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/PersonalView.fxml"));
                personalRoot = loader.load();
                personalController = loader.getController();
                personalController.setMainController(this);
                personalController.refreshProfile(userName, uid, joinTime, permission, avatarPath);
                personalController.refreshStats(postCount, viewCount, likeCount, commentCount);
            }
            contentPane.getChildren().setAll(personalRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showEditPersonalPanel() {
        try {
            if (editRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/EditPersonalView.fxml"));
                editRoot = loader.load();
                editPersonalController = loader.getController();
                editPersonalController.setMainController(this);
            }
            editPersonalController.fillForm(userName, uid, joinTime, permission, avatarPath);
            contentPane.getChildren().setAll(editRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showMyPostsPanel() {
        showListPanel("我发的", buildMyPosts());
    }

    public void showHistoryPanel() {
        showListPanel("历史浏览", buildHistory());
    }

    public void showFavoritesPanel() {
        showListPanel("收藏", buildFavorites());
    }

    private void showListPanel(String title, List<RecordCard> items) {
        try {
            if (listRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/ListView.fxml"));
                listRoot = loader.load();
                listController = loader.getController();
                listController.setMainController(this);
            }
            listController.setPanelTitle(title);
            listController.setItems(items);
            contentPane.getChildren().setAll(listRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void backToPersonal() {
        showPersonalPanel();
    }

    public void applyProfile(String newName, String newUid, String newJoinTime,
                             String newPermission, String newAvatarPath) {
        this.userName = newName;
        this.uid = newUid;
        this.joinTime = newJoinTime;
        this.permission = newPermission;
        this.avatarPath = newAvatarPath;

        if (personalController != null) {
            personalController.refreshProfile(userName, uid, joinTime, permission, avatarPath);
        }
    }

    // 右上角“设置”“管理员”按钮占位
    public void handleSettings() {
        // 空函数占位
    }

    public void handleAdmin() {
        // 空函数占位
        showAdminPanel();
    }

    private List<RecordCard> buildMyPosts() {
        List<RecordCard> list = new ArrayList<>();
        list.add(new RecordCard("小刻", "如何写一个 JavaFX 个人中心", "128", "24", "2026-06-10 18:20"));
        list.add(new RecordCard("小刻", "面板切换与 FXML 管理", "201", "31", "2026-06-09 09:11"));
        list.add(new RecordCard("小刻", "简单蓝色系 UI 设计", "87", "12", "2026-06-08 21:03"));
        return list;
    }

    private List<RecordCard> buildHistory() {
        List<RecordCard> list = new ArrayList<>();
        list.add(new RecordCard("阿明", "Spring Boot 入门", "860", "91", "2026-06-11 15:40"));
        list.add(new RecordCard("小夏", "数据库设计规范", "430", "37", "2026-06-11 20:18"));
        list.add(new RecordCard("林舟", "Java 集合实战", "1120", "145", "2026-06-12 08:55"));
        return list;
    }

    private List<RecordCard> buildFavorites() {
        List<RecordCard> list = new ArrayList<>();
        list.add(new RecordCard("夜行", "前端页面布局技巧", "532", "64", "2026-06-10 12:30"));
        list.add(new RecordCard("清风", "高质量代码风格", "290", "48", "2026-06-09 19:05"));
        list.add(new RecordCard("白茶", "JavaFX 控件速查", "777", "98", "2026-06-12 10:22"));
        return list;
    }


    // endregion


    // region 管理员界面
    public void showAdminPanel() {
        try {
            if (adminRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ceobe/canteenclient/FoodImportView.fxml"));

                adminController = new AdminController();
                adminController.setMainController(this);

                loader.setController(adminController);
                adminRoot = loader.load();
            }
            contentPane.getChildren().setAll(adminRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // endregion
}