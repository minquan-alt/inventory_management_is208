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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import jakarta.servlet.http.HttpSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


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
                // Gọi thẳng service
                User user = authService.authenticate(username, password, session);
                UserResponse userResponse = userService.mapUserResponse(user);
                
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader;
                        Parent root;
                        Stage stage = (Stage) loginButton.getScene().getWindow();
                        switch (user.getRole()) {
                            case ROLE_PRODUCT_MANAGEMENT:
                                loader = new FXMLLoader(getClass().getResource("/views/GUI/DashBoardQLKGUI.fxml"));
                                root = loader.load();
                                DashBoardProductManagerController managerController = loader.getController();
                                // productController.initData(userResponse);
                                break;

                            case ROLE_HUMAN_MANAGEMENT:
                                loader = new FXMLLoader(getClass().getResource("/views/GUI/NhanVienGUI.fxml"));
                                root = loader.load();
                                HumanManagerController humanController = loader.getController();
                                humanController.initData(userService, userResponse);
                                break;

                            case ROLE_RECEIPT:
                                loader = new FXMLLoader(getClass().getResource("/views/GUI/DashBoardNVNKGUI.fxml"));
                                root = loader.load();
                                DashBoardReceiptController receiptController = loader.getController();
                                // receiptController.initData(userResponse);
                                break;

                            case ROLE_ISSUE:
                                loader = new FXMLLoader(getClass().getResource("/views/GUI/DashBoardNVXKGUI.fxml"));
                                root = loader.load();
                                DashBoardIssueController issueController = loader.getController();
                                issueController.initData(userResponse, inventoryService);
                                break;

                            default:
                                throw new IllegalStateException("Unexpected value: " + user.getRole());
                        }
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (IOException e) {
                        System.out.print(e);
                    }
                });
            } catch (AppException e) {
                AlertUtil.showError(e.getMessage());
            }
    }

    @FXML
    public void clickForgotPassword(ActionEvent event) {
        // xử lý sign up tại đây
        System.out.println("Forgot password clicked!");
    }    
}