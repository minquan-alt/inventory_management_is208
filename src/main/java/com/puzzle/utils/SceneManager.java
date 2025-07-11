package com.puzzle.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.context.ConfigurableApplicationContext;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    private static ConfigurableApplicationContext springContext;

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }

    public static void switchScene(String fxmlPath, Node sourceButton, Object... args) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));

        if (springContext != null) {
            loader.setControllerFactory(springContext::getBean);
        }

        Parent root = loader.load();
        
        Object controller = loader.getController();
       if (controller != null && hasInitDataMethod(controller, args)) {
            passDataToController(controller, args);
        }
        
        switchScene(root, sourceButton);
    }

    private static void switchScene(Parent root, Node sourceButton) {
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
        try {
            // Tìm method initData với số parameter phù hợp
            Method initDataMethod = Arrays.stream(controller.getClass().getMethods())
                .filter(method -> method.getName().equals("initData"))
                .filter(method -> method.getParameterCount() == args.length)
                .findFirst()
                .orElse(null);
                
            if (initDataMethod != null) {
                initDataMethod.setAccessible(true);
                initDataMethod.invoke(controller, args);
            } else {
                System.err.println("No initData method found with " + args.length + " parameters");
            }
        } catch (Exception e) {
            System.err.println("Could not pass data to controller: " + e.getMessage());
            e.printStackTrace();
        }
    }
}