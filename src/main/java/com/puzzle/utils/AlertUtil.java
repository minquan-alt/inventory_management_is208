package com.puzzle.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertUtil {

    public static void showError(String message) {
        showAlert(AlertType.ERROR, "Lỗi", message);
    }
    public static void showError(String title, String message) {
        showAlert(AlertType.ERROR, title, message);
    }
    public static void showInfo(String message) {
        showAlert(AlertType.INFORMATION, "Thông báo", message);
    }
    public static void showInfo(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message);
    }
    public static void showWarning(String message) {
        showAlert(AlertType.WARNING, "Cảnh báo", message);
    }
    public static void showWarning(String title, String message) {
        showAlert(AlertType.WARNING, title, message);
    }


    private static void showAlert(AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}