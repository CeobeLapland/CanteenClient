package ceobe.canteenclient;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // 隐藏 libpng 颜色配置警告
        //System.setProperty("sun.awt.image.png.disableICCProfileWarning", "true");

        Application.launch(MainApplication.class, args);
    }
}
