package com.puzzle;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
public class App extends Application { 

    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Khởi động Spring Boot trong background
        springContext = SpringApplication.run(App.class);
    }
    
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        // load login view

        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
        loginLoader.setControllerFactory(springContext::getBean);
        Scene loginScene = new Scene(loginLoader.load(), 1200, 700);
        // loginScene.getStylesheets().add(App.class.getResource("/com/project/desktop/css/style.css").toExternalForm());

        primaryStage.setUserData(this);

        primaryStage.setTitle("IVM App - Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }

    public void showMainView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 700);
        // scene.getStylesheets().add(App.class.getResource("/com/project/desktop/css/style.css").toExternalForm());
        primaryStage.setTitle("IVM App - Main");
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args); // JavaFX gọi init() và start()
    }
}