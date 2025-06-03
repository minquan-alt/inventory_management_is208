package com.puzzle.controller.client_controller;

import com.puzzle.dto.request.CreateUserRequest;
import com.puzzle.entity.User.Role;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class HumanAddController {
    @FXML private TextField txtName;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Role> cmbRole;

    private CreateUserRequest addUser;

    @FXML
    public void initialize() {
        addUser = new CreateUserRequest();
        cmbRole.getItems().addAll(Role.values());
        cmbRole.setConverter(new StringConverter<>() {
            @Override
            public String toString(Role role) {
                if (role == null) return "";
                switch (role) {
                    case ROLE_PRODUCT_MANAGEMENT: return "Quản lý sản phẩm";
                    case ROLE_HUMAN_MANAGEMENT: return "Quản lý nhân sự";
                    case ROLE_RECEIPT: return "Nhập kho";
                    case ROLE_ISSUE: return "Xuất kho";
                    default: return role.name();
                }
            }

            @Override
            public Role fromString(String string) {
                return Role.valueOf(string);
            }
        });
    }

    public CreateUserRequest getAddedUser() {
        addUser.setName(txtName.getText());
        addUser.setUsername(txtUsername.getText());
        addUser.setPassword(txtPassword.getText());
        addUser.setRole(cmbRole.getValue());
        return addUser;
    }
}
