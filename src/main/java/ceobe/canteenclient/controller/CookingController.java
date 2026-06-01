package ceobe.canteenclient.controller;

import ceobe.canteenclient.entity.IngredientType;
import javafx.application.Platform;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;

import ceobe.canteenclient.entity.IngredientMeta;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CookingController {

    @FXML private BorderPane rootPane;
    @FXML private StackPane potStack;
    @FXML private FlowPane potFlow;
    @FXML private Button startCookButton;
    @FXML private TextArea recipeArea;

    //@FXML private Button allTagButton;
    //@FXML private Button customTagButton;

    @FXML private ScrollPane ingredientScroll;
    @FXML private FlowPane ingredientFlow;

    @FXML
    private FlowPane tagFlow;

    @FXML private HBox customInputBar;
    @FXML private TextField customIngredientField;
    @FXML private Button showCustomInputButton;

    private IngredientType currentFilter = null;
    // 当前的食材过滤模式，null 代表显示全部

    // region css 样式定义
    private static final String CARD_STYLE =
            "-fx-background-color: linear-gradient(to bottom, #f7fbff, #dfeeff);" +
                    "-fx-background-radius: 14;" +
                    "-fx-border-color: #8bb7ff;" +
                    "-fx-border-radius: 14;" +
                    "-fx-padding: 8 14 8 14;" +
                    "-fx-cursor: hand;";

    private static final String CARD_STYLE_HOVER =
            "-fx-background-color: linear-gradient(to bottom, #ffffff, #d4e8ff);" +
                    "-fx-background-radius: 14;" +
                    "-fx-border-color: #5f9dff;" +
                    "-fx-border-radius: 14;" +
                    "-fx-padding: 8 14 8 14;" +
                    "-fx-cursor: hand;";

    private static final String TAG_SELECTED =
            "-fx-background-color: #3f8cff;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 14;" +
                    "-fx-padding: 8 16 8 16;" +
                    "-fx-cursor: hand;";

    private static final String TAG_NORMAL =
            "-fx-background-color: #dceaff;" +
                    "-fx-text-fill: #1f4f99;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 14;" +
                    "-fx-padding: 8 16 8 16;" +
                    "-fx-cursor: hand;";
    // endregion


    private MainController mainController;
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


    @FXML
    private void initialize() {
        rootPane.setStyle("-fx-background-color: #f4f8ff;");

        recipeArea.setText("""
                【食谱占位】
                把食材拖进左侧“锅”里，然后点击“开始做饭”。
                
                这里后续可以接你的 AI 接口，自动根据已放入的食材生成食谱。
                """);

        startCookButton.setMaxWidth(Double.MAX_VALUE);

        customInputBar.setVisible(false);
        customInputBar.setManaged(false);

        ingredientScroll.setFitToWidth(true);
        ingredientScroll.setPannable(true);

        ingredientFlow.setPadding(new Insets(12));
        potFlow.setPadding(new Insets(8));

        initTags();
        initSampleIngredients();
        setupDragTargets();
    }

    private void initTags() {

        tagFlow.getChildren().clear();
        Button allButton = createTagButton("全部", null);
        tagFlow.getChildren().add(allButton);

        for (IngredientType type : IngredientType.values()) {
            Button btn = createTagButton(
                    type.getDisplayName(),
                    type
            );
            tagFlow.getChildren().add(btn);
        }
    }

    private Button createTagButton(
            String text,
            IngredientType type
    ) {
        Button btn = new Button(text);

        btn.setStyle(
                type == currentFilter
                        || (type == null && currentFilter == null)
                        ? TAG_SELECTED
                        : TAG_NORMAL
        );
        btn.setOnAction(e -> {

            currentFilter = type;
            updateTagStyles();
            refreshIngredientVisibility();
        });

        return btn;
    }

    private void updateTagStyles() {

        for (Node node : tagFlow.getChildren()) {
            if (!(node instanceof Button btn))
                continue;

            String text = btn.getText();
            boolean selected;

            if ("全部".equals(text)) {
                selected = currentFilter == null;
            } else {
                selected = false;
                for (IngredientType type : IngredientType.values()) {
                    if (type.getDisplayName().equals(text)) {
                        selected = type == currentFilter;
                        break;
                    }
                }
            }
            btn.setStyle(
                    selected
                            ? TAG_SELECTED
                            : TAG_NORMAL
            );
        }
    }

    private void initSampleIngredients() {
        String[] samples = {
                "番茄", "鸡蛋", "土豆", "牛肉", "洋葱", "青椒",
                "豆腐", "米饭", "虾仁", "菌菇", "面条", "青菜",
                "胡萝卜", "玉米", "鸡胸肉", "香菇", "黄瓜", "茄子"
        };

        ingredientFlow.getChildren().clear();

        addIngredient("番茄", IngredientType.VEGETABLE);
        addIngredient("土豆", IngredientType.VEGETABLE);
        addIngredient("黄瓜", IngredientType.VEGETABLE);

        addIngredient("牛肉", IngredientType.MEAT);
        addIngredient("猪肉", IngredientType.MEAT);

        addIngredient("虾仁", IngredientType.SEAFOOD);
        addIngredient("螃蟹", IngredientType.SEAFOOD);

        addIngredient("牛奶", IngredientType.DAIRY);

        addIngredient("米饭", IngredientType.GRAIN);
        addIngredient("面条", IngredientType.GRAIN);

        addIngredient("苹果", IngredientType.FRUIT);

        /*
        for (String s : samples) {
            //ingredientFlow.getChildren().add(createIngredientCard(s, false));
            ingredientFlow.getChildren().add(
                    createIngredientCard(
                            "番茄",
                            IngredientType.VEGETABLE
                    )
            );
            ingredientFlow.getChildren().add(
                    createIngredientCard(
                            "牛肉",
                            IngredientType.MEAT
                    )
            );
            ingredientFlow.getChildren().add(
                    createIngredientCard(
                            "虾仁",
                            IngredientType.SEAFOOD
                    )
            );
        }*/
        refreshIngredientVisibility();
    }

    private void addIngredient(String name, IngredientType type) {
        ingredientFlow.getChildren().add(
                createIngredientCard(name, type)
        );
    }

    private void setupDragTargets() {
        setupDropTarget(potStack, potFlow, true);
        setupDropTarget(ingredientScroll, ingredientFlow, false);
    }

    private void setupDropTarget(Node targetNode, FlowPane targetFlow, boolean targetIsPot) {
        targetNode.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasString() && draggingCard != null) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        targetNode.setOnDragEntered(e -> {
            if (draggingCard != null) {
                targetNode.setStyle(targetHighlightStyle(targetIsPot));
            }
            e.consume();
        });

        targetNode.setOnDragExited(e -> {
            targetNode.setStyle(targetNormalStyle(targetIsPot));
            e.consume();
        });

        targetNode.setOnDragDropped(e -> {
            boolean success = false;
            if (draggingCard != null && e.getDragboard().hasString()) {
                moveCardToFlow(draggingCard, targetFlow);
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }

    private String targetNormalStyle(boolean pot) {
        return pot
                ? "-fx-background-color: linear-gradient(to bottom, #fdfefe, #eaf3ff);" +
                "-fx-border-color: #8bb7ff;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 18;" +
                "-fx-background-radius: 18;"
                : "-fx-background: transparent;" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: #cfe0ff;" +
                "-fx-border-radius: 16;" +
                "-fx-background-radius: 16;";
    }

    private String targetHighlightStyle(boolean pot) {
        return pot
                ? "-fx-background-color: linear-gradient(to bottom, #f7fbff, #dfeeff);" +
                "-fx-border-color: #3f8cff;" +
                "-fx-border-width: 3;" +
                "-fx-border-radius: 18;" +
                "-fx-background-radius: 18;"
                : "-fx-background: transparent;" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: #3f8cff;" +
                "-fx-border-radius: 16;" +
                "-fx-background-radius: 16;";
    }

    private StackPane createIngredientCard(String name, IngredientType type) {
        Label label = new Label(name);
        label.setStyle("-fx-text-fill: #1f4f99; -fx-font-weight: bold; -fx-font-size: 13px;");

        StackPane card = new StackPane(label);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(0));
        card.setStyle(CARD_STYLE);

        IngredientMeta meta = new IngredientMeta(name, type);
        card.setUserData(meta);

        card.setOnMouseEntered(e -> card.setStyle(CARD_STYLE_HOVER));
        card.setOnMouseExited(e -> card.setStyle(CARD_STYLE));

        card.setOnDragDetected(e -> {
            if (card.getParent() == null) return;

            draggingCard = card;

            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(meta.name);
            db.setContent(content);

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            db.setDragView(card.snapshot(params, null));

            e.consume();
        });

        card.setOnDragDone(e -> {
            draggingCard = null;
            e.consume();
        });

        return card;
    }

    private Node draggingCard = null;

    private void moveCardToFlow(Node card, FlowPane targetFlow) {
        if (card.getParent() == targetFlow) {
            return;
        }

        Parent parent = card.getParent();
        if (parent instanceof FlowPane flowPane) {
            flowPane.getChildren().remove(card);
        } else if (parent instanceof VBox vBox) {
            vBox.getChildren().remove(card);
        } else if (parent != null) {
            ((Region) parent).getChildrenUnmodifiable(); // 占位，避免某些 IDE 警告；实际不会走到这里
        }

        targetFlow.getChildren().add(card);

        if (targetFlow == ingredientFlow) {
            refreshSingleIngredientVisibility(card);
        } else {
            card.setVisible(true);
            card.setManaged(true);
        }
    }

    private void refreshIngredientVisibility() {
        for (Node node : ingredientFlow.getChildren()) {
            refreshSingleIngredientVisibility(node);
        }
    }

    private void refreshSingleIngredientVisibility(Node node) {
        if (!(node.getUserData() instanceof IngredientMeta meta)) {
            node.setVisible(true);
            node.setManaged(true);
            return;
        }

        boolean show;
        if (currentFilter == null) {
            show = true;
        } else {
            show = meta.type == currentFilter;
        }

        node.setVisible(show);
        node.setManaged(show);
    }

    /*@FXML
    private void onFilterAll() {
        currentFilter = null;
        allTagButton.setStyle(TAG_SELECTED);
        customTagButton.setStyle(TAG_NORMAL);
        refreshIngredientVisibility();
    }

    @FXML
    private void onFilterCustom() {
        currentFilter = IngredientType.CUSTOM;
        allTagButton.setStyle(TAG_NORMAL);
        customTagButton.setStyle(TAG_SELECTED);
        refreshIngredientVisibility();
    }*/

    @FXML
    private void onShowCustomInput() {
        boolean show = !customInputBar.isVisible();
        customInputBar.setVisible(show);
        customInputBar.setManaged(show);

        if (show) {
            Platform.runLater(() -> customIngredientField.requestFocus());
        }
    }

    @FXML
    private void onConfirmCustomIngredient() {
        String name = customIngredientField.getText() == null ? "" : customIngredientField.getText().trim();
        if (name.isEmpty()) {
            return;
        }

        StackPane card = createIngredientCard(name, IngredientType.CUSTOM);
        ingredientFlow.getChildren().add(card);

        customIngredientField.clear();
        customInputBar.setVisible(false);
        customInputBar.setManaged(false);

        refreshIngredientVisibility();
    }

    @FXML
    private void onStartCook() {
        List<String> ingredients = potFlow.getChildren().stream()
                .filter(n -> n.getUserData() instanceof IngredientMeta)
                .map(n -> ((IngredientMeta) n.getUserData()).name)
                .collect(Collectors.toList());

        if (ingredients.isEmpty()) {
            recipeArea.setText("""
                    还没有放食材进去。
                    
                    先把右侧的食材拖到左边的“锅”里，再点击“开始做饭”。
                    """);
            return;
        }

        recipeArea.setText(generateRecipe(ingredients));
    }

    private String generateRecipe(List<String> ingredients) {
        // 这里是“内置 AI 功能”的占位实现：
        // 你后续可以把这个方法替换成调用你自己的大模型接口、OpenAI API、或本地模型的结果。
        String main = ingredients.get(0);
        String title = ingredients.size() == 1
                ? main + "简易料理"
                : main + "家常组合";

        String ingredientLine = String.join("、", ingredients);

        StringBuilder sb = new StringBuilder();
        sb.append("【AI 生成食谱占位】\n");
        sb.append("菜名：").append(title).append("\n");
        sb.append("已放入锅中的食材：").append(ingredientLine).append("\n\n");
        sb.append("步骤：\n");
        sb.append("1. 预热锅具，准备 ").append(main).append("。\n");
        sb.append("2. 将食材按耐熟程度依次下锅，翻炒均匀。\n");

        if (ingredients.size() >= 2) {
            sb.append("3. 根据食材组合补充少量盐、酱油或清水。\n");
        } else {
            sb.append("3. 加少量调味料后小火收味。\n");
        }

        sb.append("4. 炒至熟透后装盘。\n\n");
        sb.append("你可以把这个方法直接替换成真实 AI 接口调用。");

        return sb.toString();
    }
}