package ceobe.canteenclient;

import ceobe.canteenclient.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("正在启动学校食堂数据库...");

        FXMLLoader mainLoader;
        try{
            mainLoader = new FXMLLoader(MainApplication.class.getResource("/ceobe/canteenclient/MainWindow.fxml"));
        } catch (Exception e){
            System.out.println("加载 FXML 文件时发生错误: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        BorderPane root = mainLoader.load();

        MainController controller = mainLoader.getController();
        controller.setStage(stage);

        Scene scene = new Scene(root, 1000, 800);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setScene(scene);
        //stage.setTitle("学校食堂数据库");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

/*
            <!-- JavaFX 运行插件，零模块化专用 -->
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>com.example.Main</mainClass> <!-- 这里改成你自己的主类全类名！ -->
                </configuration>
            </plugin>
 */