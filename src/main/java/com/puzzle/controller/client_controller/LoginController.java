package com.puzzle.controller.client_controller;

import java.io.IOException;

import com.puzzle.config.FXSessionManager;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.entity.User;
import com.puzzle.exception.AppException;
import com.puzzle.service.AuthenticationService;
import com.puzzle.service.InventoryService;
import com.puzzle.service.ProductService;
import com.puzzle.service.UserService;
import com.puzzle.utils.AlertUtil;
import com.puzzle.utils.SceneManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import jakarta.servlet.http.HttpSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


@Controller // Đánh dấu là Spring Bean
public class LoginController {
    @Autowired 
    private AuthenticationService authService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;


    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;


    @FXML
    public void clickSignUp(ActionEvent event) {
        FXSessionManager.startRequestContext();
        String username = usernameField.getText();
        String password = passwordField.getText();
        HttpSession session = FXSessionManager.getSession();

        try {
        User user = authService.authenticate(username, password, session);
        UserResponse userResponse = userService.mapUserResponse(user);
        System.out.print(userResponse);
        
        String fxmlPath;
        Object[] initArgs;
        switch (user.getRole()) {
            case ROLE_PRODUCT_MANAGEMENT:
                fxmlPath = "/views/GUI/DashBoardQLKGUI.fxml";
                initArgs = new Object[]{userResponse}; // CHỈ truyền data
                break;
            case ROLE_HUMAN_MANAGEMENT:
                fxmlPath = "/views/GUI/NhanVienGUI.fxml";
                initArgs = new Object[]{userResponse}; // CHỈ truyền data
                break;
            case ROLE_RECEIPT:
                fxmlPath = "/views/GUI/DashBoardNVNKGUI.fxml";
                initArgs = new Object[]{userResponse}; // CHỈ truyền data
                break;
            case ROLE_ISSUE:
                fxmlPath = "/views/GUI/DashBoardNVXKGUI.fxml";
                initArgs = new Object[]{userResponse}; // CHỈ truyền data
                break;
            default:
                throw new IllegalStateException("Unexpected role: " + user.getRole());
        }
        
        // Dùng SceneManager để đảm bảo Spring inject dependencies
        SceneManager.switchScene(fxmlPath, loginButton, initArgs);
        
    } catch (AppException | IOException e) {
        AlertUtil.showError(e.getMessage());
    }
        
        // try {
        //     User user = authService.authenticate(username, password, session);
        //     UserResponse userResponse = userService.mapUserResponse(user);
            
        //     String fxmlPath;
        //     Object[] initArgs;
        //     switch (user.getRole()) {
        //         case ROLE_PRODUCT_MANAGEMENT:
        //             fxmlPath = "/views/GUI/DashBoardQLKGUI.fxml";
        //             initArgs = new Object[]{userResponse, inventoryService, productService};
        //             break;
        //         case ROLE_HUMAN_MANAGEMENT:
        //             fxmlPath = "/views/GUI/NhanVienGUI.fxml";
        //             initArgs = new Object[]{userService, userResponse};
        //             break;
        //         case ROLE_RECEIPT:
        //             fxmlPath = "/views/GUI/DashBoardNVNKGUI.fxml";
        //             initArgs = new Object[]{userResponse, inventoryService};
        //             break;
        //         case ROLE_ISSUE:
        //             fxmlPath = "/views/GUI/DashBoardNVXKGUI.fxml";
        //             initArgs = new Object[]{userResponse, inventoryService};
        //             break;
        //         default:
        //             throw new IllegalStateException("Unexpected role: " + user.getRole());
        //     }
            
        //     // Sử dụng SceneManager để chuyển cảnh (dùng loginButton làm sourceNode)
        //     SceneManager.switchScene(fxmlPath, loginButton, initArgs);
            
        // } catch (AppException | IOException e) {
        //     AlertUtil.showError(e.getMessage());
        // }
    }

    @FXML
    public void clickForgotPassword(ActionEvent event) {
        // xử lý sign up tại đây
        System.out.println("Forgot password clicked!");
    }    
}