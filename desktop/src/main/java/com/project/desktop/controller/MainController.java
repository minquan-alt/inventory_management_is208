package com.project.desktop.controller;

import com.project.desktop.service.HelloService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainController{
    @FXML private Label messageLabel;
    @FXML private Button reloadButton;

    private HelloService helloService;

    @FXML
    private void initialize() {
        helloService = new HelloService("http://localhost:8080/api/hello");
        fetchAndDisplay();
        reloadButton.setOnAction(e -> fetchAndDisplay());
    }

    private void fetchAndDisplay() {
        messageLabel.setText("Loading");

        new Thread(() -> {
           String result = helloService.fetchHello();
           Platform.runLater(() -> messageLabel.setText(result));
        }).start();
    }
}

