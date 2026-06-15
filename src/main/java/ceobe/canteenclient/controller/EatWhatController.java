package ceobe.canteenclient.controller;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;

public class EatWhatController implements Initializable {

    @FXML private Canvas wheelCanvas;
    @FXML private Spinner<Integer> sectorSpinner;
    @FXML private Button drawButton;
    @FXML private TextArea resultPreviewArea;
    @FXML private FlowPane tagFlowPane;
    @FXML private Button confirmButton;
    @FXML private Button clearButton;

    private final Random random = new Random();

    private final List<String> foodNames = new ArrayList<>();
    private final List<String> loadedFoods = new ArrayList<>();
    private final List<String> history = new ArrayList<>();
    private final List<ToggleButton> tagButtons = new ArrayList<>();

    private final DoubleProperty wheelRotation = new SimpleDoubleProperty(0.0);

    private Timeline spinTimeline;
    private int sectorCount = 6;

    private static final String TAG_NORMAL_STYLE =
            "-fx-background-color: #EAF4FF;" +
                    "-fx-text-fill: #24507A;" +
                    "-fx-background-radius: 999;" +
                    "-fx-border-color: #B9D8FF;" +
                    "-fx-border-radius: 999;" +
                    "-fx-font-size: 13px;" +
                    "-fx-padding: 6 12 6 12;";

    private static final String TAG_SELECTED_STYLE =
            "-fx-background-color: linear-gradient(to bottom, #2F80ED, #6AAEFF);" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 999;" +
                    "-fx-border-color: #2F80ED;" +
                    "-fx-border-radius: 999;" +
                    "-fx-font-size: 13px;" +
                    "-fx-padding: 6 12 6 12;";


    private MainController mainController;
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sectorSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 12, 6));
        sectorSpinner.setEditable(false);
        sectorSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                updateSectorCount(newValue);
            }
        });

        wheelRotation.addListener((obs, oldValue, newValue) -> drawWheel());
        wheelCanvas.widthProperty().addListener((obs, oldValue, newValue) -> drawWheel());
        wheelCanvas.heightProperty().addListener((obs, oldValue, newValue) -> drawWheel());

        System.out.println(loadedFoods.size()+" loadedFoods "+foodNames.size()+" foodNames");

        buildTags();
        updateSectorCount(6);
        refreshPreview();
        drawWheel();
    }

    private void buildTags() {
        List<String> tags = Arrays.asList(
                "川菜", "粤菜", "湘菜", "鲁菜", "苏菜", "浙菜", "闽菜", "徽菜",
                "辣", "清淡", "下饭", "低脂", "高蛋白", "素食", "海鲜", "牛肉",
                "鸡肉", "面食", "米饭", "汤类", "早餐", "午餐", "晚餐", "夜宵",
                "甜口", "咸口", "炸物", "烧烤", "咖喱", "酸甜"
        );

        tagFlowPane.getChildren().clear();
        tagButtons.clear();

        for (String tag : tags) {
            ToggleButton button = new ToggleButton(tag);
            button.setPrefWidth(78);
            button.setPrefHeight(34);
            button.setStyle(TAG_NORMAL_STYLE);
            button.selectedProperty().addListener((obs, oldValue, selected) -> {
                button.setStyle(selected ? TAG_SELECTED_STYLE : TAG_NORMAL_STYLE);
            });
            tagButtons.add(button);
            tagFlowPane.getChildren().add(button);
        }
    }

    @FXML
    private void handleConfirmTags() {
        List<String> tags = getSelectedTags();
        List<String> foods = getFoodForTable(tags);

        if (foods == null || foods.isEmpty()) {
            showAlert(AlertType.WARNING, "没有返回食物数据", "请检查外部 API 是否正常返回数据。");
            return;
        }

        loadedFoods.clear();
        loadedFoods.addAll(foods);

        for (int i = 0; i < sectorCount; i++) {
            String value = i < foods.size() ? safeText(foods.get(i)) : "";
            if (i < foodNames.size()) {
                foodNames.set(i, value);
            } else {
                foodNames.add(value);
            }
        }

        refreshPreview();
        drawWheel();
        showAlert(AlertType.INFORMATION, "加载成功", "已根据当前标签加载食物数据。");
    }

    @FXML
    private void handleClearTags() {
        for (ToggleButton button : tagButtons) {
            button.setSelected(false);
        }
        showAlert(AlertType.INFORMATION, "已清空", "标签选择已经清空。");
    }

    @FXML
    private void handleDraw() {
        if (spinTimeline != null && spinTimeline.getStatus() == Animation.Status.RUNNING) {
            return;
        }

        if (!validateFoodNames()) {
            return;
        }

        int targetIndex = random.nextInt(sectorCount);
        double anglePerSector = 360.0 / sectorCount;

        // 让最终停在目标扇形正中间
        double currentNormalized = normalizeAngle(wheelRotation.get());
        double targetNormalized = normalizeAngle(-targetIndex * anglePerSector);
        double deltaToTarget = normalizeAngle(targetNormalized - currentNormalized);

        int extraRounds = 5 + random.nextInt(4); // 5~8 圈，动画更自然
        double endRotation = wheelRotation.get() + extraRounds * 360.0 + deltaToTarget;

        drawButton.setDisable(true);

        spinTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(wheelRotation, wheelRotation.get())),
                new KeyFrame(
                        Duration.seconds(4.2),
                        new KeyValue(
                                wheelRotation,
                                endRotation,
                                Interpolator.SPLINE(0.12, 0.85, 0.18, 1.0)
                        )
                )
        );

        spinTimeline.setOnFinished(event -> {
            drawButton.setDisable(false);

            int landedIndex = getSectorUnderPointer(wheelRotation.get());
            String result = safeText(foodNames.get(landedIndex));

            history.add(0, "抽中：第 " + (landedIndex + 1) + " 格 - " + result);
            if (history.size() > 8) {
                history.remove(history.size() - 1);
            }

            refreshPreview();
            drawWheel();

            showAlert(AlertType.INFORMATION, "抽取结果", "本次抽中：\n" + result);
        });

        spinTimeline.playFromStart();
    }

    @FXML
    private void handleWheelClick(MouseEvent event) {
        if (spinTimeline != null && spinTimeline.getStatus() == Animation.Status.RUNNING) {
            return;
        }

        double w = wheelCanvas.getWidth();
        double h = wheelCanvas.getHeight();
        double cx = w / 2.0;
        double cy = h / 2.0;
        double radius = Math.min(w, h) * 0.42;

        double dx = event.getX() - cx;
        double dy = event.getY() - cy;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > radius) {
            return;
        }

        int index = getSectorIndexAtPoint(event.getX(), event.getY(), wheelRotation.get());
        if (index < 0 || index >= foodNames.size()) {
            return;
        }

        String current = safeText(foodNames.get(index));
        TextInputDialog dialog = new TextInputDialog(current.equals("未命名") ? "" : current);
        dialog.setTitle("编辑食物名称");
        dialog.setHeaderText("修改第 " + (index + 1) + " 个扇形的名称");
        dialog.setContentText("请输入食物名称：");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String value = result.get() == null ? "" : result.get().trim();
            if (index < foodNames.size()) {
                foodNames.set(index, value);
            }
            refreshPreview();
            drawWheel();
        }
    }

    private void updateSectorCount(int newCount) {
        int oldCount = sectorCount;
        sectorCount = newCount;

        System.out.println(foodNames.size()+" "+loadedFoods.size());

        if (newCount > oldCount) {
            for (int i = oldCount; i < newCount; i++) {
                String value = i < loadedFoods.size() ? safeText(loadedFoods.get(i)) : "";
                foodNames.add(value);
            }
        } else if (newCount < oldCount) {
            while (foodNames.size() > newCount) {
                foodNames.remove(foodNames.size() - 1);
            }
        }

        drawWheel();
        refreshPreview();
    }

    private void drawWheel() {
        GraphicsContext gc = wheelCanvas.getGraphicsContext2D();
        double w = wheelCanvas.getWidth();
        double h = wheelCanvas.getHeight();

        gc.clearRect(0, 0, w, h);

        if (w <= 0 || h <= 0 || sectorCount <= 0) {
            return;
        }

        double cx = w / 2.0;
        double cy = h / 2.0;
        double radius = Math.min(w, h) * 0.42;
        double currentRotation = normalizeAngle(wheelRotation.get());
        double anglePerSector = 360.0 / sectorCount;
        int highlightedIndex = getSectorUnderPointer(currentRotation);

        // 先画一个淡淡的底圈
        gc.setFill(Color.web("#F5FAFF"));
        gc.fillOval(cx - radius - 10, cy - radius - 10, (radius + 10) * 2, (radius + 10) * 2);
        gc.setStroke(Color.web("#CFE4FF"));
        gc.setLineWidth(2);
        gc.strokeOval(cx - radius - 10, cy - radius - 10, (radius + 10) * 2, (radius + 10) * 2);

        for (int i = 0; i < sectorCount; i++) {
            double startAngle = currentRotation - 90.0 - anglePerSector / 2.0 + i * anglePerSector;

            Color fill = (i == highlightedIndex)
                    ? highlightColor(i)
                    : baseColor(i);

            gc.setFill(fill);
            gc.fillArc(
                    cx - radius, cy - radius,
                    radius * 2, radius * 2,
                    startAngle, anglePerSector,
                    ArcType.ROUND
            );

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeArc(
                    cx - radius, cy - radius,
                    radius * 2, radius * 2,
                    startAngle, anglePerSector,
                    ArcType.ROUND
            );

            // 扇形文字
            String label = trimLabel(safeText(foodNames.isEmpty()?"":foodNames.get(i)));
            double midAngle = Math.toRadians(startAngle + anglePerSector / 2.0);
            double textRadius = radius * 0.62;
            double tx = cx + Math.cos(midAngle) * textRadius;
            double ty = cy + Math.sin(midAngle) * textRadius;

            gc.setFill(Color.web("#1D3F63"));
            gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, fontSizeForSector(sectorCount)));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(label, tx, ty, radius * 0.8);
        }

        // 中心圆
        double hubRadius = radius * 0.18;
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - hubRadius, cy - hubRadius, hubRadius * 2, hubRadius * 2);
        gc.setStroke(Color.web("#9BC7FF"));
        gc.setLineWidth(2);
        gc.strokeOval(cx - hubRadius, cy - hubRadius, hubRadius * 2, hubRadius * 2);

        // 指针（三角形，固定在顶部）
        gc.setFill(Color.web("#2F80ED"));
        double[] xs = {cx, cx - 13, cx + 13};
        double[] ys = {cy - radius - 8, cy - radius + 18, cy - radius + 18};
        gc.fillPolygon(xs, ys, 3);

        // 指针小圆点
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - 5, cy - radius + 4, 10, 10);
    }

    private boolean validateFoodNames() {
        for (int i = 0; i < sectorCount; i++) {
            if (i >= foodNames.size() || safeText(foodNames.get(i)).equals("未命名")) {
                showAlert(AlertType.WARNING,
                        "存在空值",
                        "第 " + (i + 1) + " 个扇形还没有填写食物名称。");
                return false;
            }
        }
        return true;
    }

    private List<String> getSelectedTags() {
        List<String> tags = new ArrayList<>();
        for (ToggleButton button : tagButtons) {
            if (button.isSelected()) {
                tags.add(button.getText());
            }
        }
        return tags;
    }

    private int getSectorIndexAtPoint(double x, double y, double rotationDegrees) {
        double w = wheelCanvas.getWidth();
        double h = wheelCanvas.getHeight();
        double cx = w / 2.0;
        double cy = h / 2.0;

        double dx = x - cx;
        double dy = y - cy;
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) {
            angle += 360.0;
        }

        double anglePerSector = 360.0 / sectorCount;
        double relative = normalizeAngle(angle - normalizeAngle(rotationDegrees) + 90.0 + anglePerSector / 2.0);
        int index = (int) Math.floor(relative / anglePerSector);

        if (index < 0) index = 0;
        if (index >= sectorCount) index = sectorCount - 1;
        return index;
    }

    private int getSectorUnderPointer(double rotationDegrees) {
        double anglePerSector = 360.0 / sectorCount;
        double relative = normalizeAngle(anglePerSector / 2.0 - normalizeAngle(rotationDegrees));
        int index = (int) Math.floor(relative / anglePerSector);
        if (index < 0) index = 0;
        if (index >= sectorCount) index = sectorCount - 1;
        return index;
    }

    private double normalizeAngle(double angle) {
        double result = angle % 360.0;
        if (result < 0) {
            result += 360.0;
        }
        return result;
    }

    private String safeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "未命名";
        }
        return text.trim();
    }

    private String trimLabel(String text) {
        if (text == null || text.isBlank()) {
            return "未命名";
        }
        String t = text.trim();
        return t.length() <= 7 ? t : t.substring(0, 7) + "…";
    }

    private double fontSizeForSector(int count) {
        if (count <= 4) return 18;
        if (count <= 6) return 16;
        if (count <= 8) return 14;
        return 12;
    }

    private Color baseColor(int index) {
        Color[] palette = {
                Color.web("#D9ECFF"),
                Color.web("#C7E2FF"),
                Color.web("#B6D8FF"),
                Color.web("#A6CEFF"),
                Color.web("#D7F0FF"),
                Color.web("#CFE8FF")
        };
        return palette[index % palette.length];
    }

    private Color highlightColor(int index) {
        Color c = baseColor(index);
        return c.brighter().brighter();
    }

    private void refreshPreview() {
        StringBuilder sb = new StringBuilder();

        sb.append("当前转盘（").append(sectorCount).append(" 格）\n");
        for (int i = 0; i < sectorCount; i++) {
            String name = i < foodNames.size() ? safeText(foodNames.get(i)) : "未命名";
            sb.append(String.format("%02d. %s%n", i + 1, name));
        }

        if (!history.isEmpty()) {
            sb.append("\n最近抽取：\n");
            for (String item : history) {
                sb.append("• ").append(item).append('\n');
            }
        }

        resultPreviewArea.setText(sb.toString());
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 把这里替换成你已经写好的外部 API。
     * 例如：
     * return foodService.getFoodForTable(tags);
     */
    private List<String> getFoodForTable(List<String> tags) {
        // 下面只是兜底示例，方便你先跑通界面。
        // 你接入自己的外部 API 后，直接替换整个方法体即可。
        return Arrays.asList(
                "宫保鸡丁", "麻婆豆腐", "番茄炒蛋", "鱼香肉丝",
                "可乐鸡翅", "红烧牛肉面", "土豆丝", "青椒肉丝",
                "酸辣汤", "白灼虾", "咖喱饭", "牛肉盖饭"
        );
    }
}