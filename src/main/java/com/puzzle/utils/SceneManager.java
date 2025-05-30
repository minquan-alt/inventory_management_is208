package com.puzzle.utils;

import java.io.IOException;
import java.util.Arrays;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SceneManager {
    public static void switchScene(String fxmlPath, Button sourceButton, Object... args) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        
        Object controller = loader.getController();
       if (controller != null && hasInitDataMethod(controller, args)) {
            passDataToController(controller, args);
        }
        
        switchScene(root, sourceButton);
    }

    private static void switchScene(Parent root, Button sourceButton) {
        Stage stage = (Stage) sourceButton.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private static boolean hasInitDataMethod(Object controller, Object... args) {
        return Arrays.stream(controller.getClass().getMethods())
                .anyMatch(method -> method.getName().equals("initData"));
    }


    private static void passDataToController(Object controller, Object... args) {
        Class<?>[] argTypes = Arrays.stream(args)
                                .map(Object::getClass)
                                .toArray(Class<?>[]::new);
        try {
            controller.getClass()
                .getMethod("initData", argTypes)
                .invoke(controller, args);
        } catch (Exception e) {
            System.err.println("Could not pass data to controller: " + e.getMessage());
        }
    }
}