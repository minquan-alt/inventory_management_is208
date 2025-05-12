package com.project.desktop;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DesktopApplication extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        // load login view

        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/com/project/desktop/views/LoginView.fxml"));
        Scene loginScene = new Scene(loginLoader.load(), 1200, 700);
        loginScene.getStylesheets().add(DesktopApplication.class.getResource("/com/project/desktop/css/style.css").toExternalForm());

        primaryStage.setUserData(this);

        primaryStage.setTitle("IVM App - Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    public void showMainView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/desktop/views/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 700);
        scene.getStylesheets().add(DesktopApplication.class.getResource("/com/project/desktop/css/style.css").toExternalForm());
        primaryStage.setTitle("IVM App - Main");
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}