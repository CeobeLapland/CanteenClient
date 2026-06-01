package ceobe.canteenclient.controller;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EatWhatController// implements Initializable
{

    // 转盘核心参数
    private static final double WHEEL_RADIUS = 280;
    private static final double CENTER_X = 300;
    private static final double CENTER_Y = 300;
    private int sectionCount = 6; // 默认6块
    private double currentRotateAngle = 0;
    private final Random random = new Random();
    private boolean isSpinning = false;

    // 蓝色系配色（区分扇形块）
    private final List<Color> sectionColors = Arrays.asList(
            Color.web("#64B5F6"), Color.web("#42A5F5"),
            Color.web("#2196F3"), Color.web("#1976D2"),
            Color.web("#1565C0"), Color.web("#0D47A1"),
            Color.web("#82B1FF"), Color.web("#448AFF"),
            Color.web("#2979FF"), Color.web("#2962FF"),
            Color.web("#1E88E5"), Color.web("#0D47A1")
    );

    // 食物数据
    private List<String> foodList;
    private final List<String> defaultFoods = Arrays.asList(
            "汉堡", "披萨", "寿司", "拉面", "炸鸡", "奶茶",
            "蛋糕", "火锅", "烧烤", "冰淇淋", "牛排", "馄饨"
    );

    // FXML注入控件
    @FXML
    private Canvas wheelCanvas;
    @FXML
    private Spinner<Integer> sectionSpinner;
    @FXML
    private Button spinButton;
    @FXML
    private TextArea resultArea;

    private MainController mainController;
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    //@Override
    @FXML
    public void initialize()  //(URL url, ResourceBundle resourceBundle)
    {
        // 初始化块数选择器（3~12块）
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 12, 6);
        sectionSpinner.setValueFactory(valueFactory);

        // 监听块数变化
        sectionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isSpinning) {
                sectionCount = newVal;
                initFoodList();
                drawWheel();
            }
        });

        // 初始化数据并绘制转盘
        initFoodList();
        drawWheel();

        // 绑定抽取按钮事件
        spinButton.setOnAction(e -> startSpinAnimation());
    }

    /**
     * 初始化食物列表（根据块数动态生成）
     */
    private void initFoodList() {
        foodList = new ArrayList<>();
        for (int i = 0; i < sectionCount; i++) {
            foodList.add(defaultFoods.get(i % defaultFoods.size()));
        }
    }

    /**
     * 绘制转盘
     */
    private void drawWheel() {
        GraphicsContext gc = wheelCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, wheelCanvas.getWidth(), wheelCanvas.getHeight());

        double anglePerSection = 360.0 / sectionCount;

        // 绘制每一个扇形块
        for (int i = 0; i < sectionCount; i++) {
            double startAngle = currentRotateAngle + i * anglePerSection;

            // 设置扇形颜色
            gc.setFill(sectionColors.get(i % sectionColors.size()));
            gc.fillArc(CENTER_X - WHEEL_RADIUS, CENTER_Y - WHEEL_RADIUS,
                    WHEEL_RADIUS * 2, WHEEL_RADIUS * 2,
                    startAngle, anglePerSection, javafx.scene.shape.ArcType.ROUND);

            // 绘制边框
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeArc(CENTER_X - WHEEL_RADIUS, CENTER_Y - WHEEL_RADIUS,
                    WHEEL_RADIUS * 2, WHEEL_RADIUS * 2,
                    startAngle, anglePerSection, javafx.scene.shape.ArcType.ROUND);

            // 绘制食物文字
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(javafx.scene.text.Font.font(16));

            double textAngle = Math.toRadians(startAngle + anglePerSection / 2);
            double textX = CENTER_X + (WHEEL_RADIUS * 0.7) * Math.cos(textAngle);
            double textY = CENTER_Y + (WHEEL_RADIUS * 0.7) * Math.sin(textAngle);
            gc.fillText(foodList.get(i), textX, textY);
        }
    }

    /**
     * 转盘旋转动画（先加速后减速）
     */
    private void startSpinAnimation() {
        if (isSpinning) return;
        isSpinning = true;
        spinButton.setDisable(true);
        resultArea.clear();

        // 随机旋转角度：5~10圈 + 随机扇形偏移
        double randomAngle = 1800 + random.nextDouble(1800) + random.nextDouble(360);
        double endAngle = currentRotateAngle + randomAngle;

        // 动画：3秒，先加速后减速
        Timeline timeline = new Timeline();
        Animation animation = new Transition() {
            {
                setCycleDuration(Duration.seconds(3));
                // 插值器：先加速后减速
                setInterpolator(Interpolator.SPLINE(0.1, 0.8, 0.3, 1.0));
            }

            @Override
            protected void interpolate(double frac) {
                currentRotateAngle = currentRotateAngle + (endAngle - currentRotateAngle) * frac;
                drawWheel();
            }
        };

        animation.setOnFinished(e -> {
            isSpinning = false;
            spinButton.setDisable(false);
            // 计算最终选中的块
            int selectedIndex = calculateSelectedIndex();
            showResult(selectedIndex);
        });

        animation.play();
    }

    /**
     * 计算最终选中的扇形索引
     */
    private int calculateSelectedIndex() {
        double normalizedAngle = (currentRotateAngle % 360 + 360) % 360;
        double anglePerSection = 360.0 / sectionCount;
        return (int) (normalizedAngle / anglePerSection);
    }

    /**
     * 展示抽取结果
     */
    private void showResult(int index) {
        String selectedFood = foodList.get(index);
        resultArea.setText("""
                🎉 抽取完成！
                ----------------
                转盘块数：%d
                选中食物：%s
                ----------------
                祝您用餐愉快！""".formatted(sectionCount, selectedFood));
    }
}