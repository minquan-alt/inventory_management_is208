package com.puzzle.controller.client_controller;

import java.util.Arrays;
import java.util.List;

import com.puzzle.dto.response.UserResponse;
import com.puzzle.entity.User.Role;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class HumanUpdateController {
    @FXML private Label lblUserId;
    @FXML private TextField txtName;
    @FXML private TextField txtUsername;
    @FXML private ComboBox<Role> cmbRole;
    @FXML private ComboBox<String> cmbStatus;

    private UserResponse editingUser;

    @FXML
    public void initialize() {
        List<String> statuses = Arrays.asList("Đang hoạt động", "Ngưng hoạt động");

        cmbStatus.getItems().addAll(statuses);
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
        cmbStatus.setConverter(new StringConverter<>() {
            @Override
            public String toString(String object) {
                return object == null ? "" : object;
            }
            @Override
            public String fromString(String string) {
                return string;
            }
        });
    }

    public void setEditingUser(UserResponse user) {
        this.editingUser = user;

        lblUserId.setText(String.valueOf(user.getId()));
        txtName.setText(user.getName());
        txtUsername.setText(user.getUsername());
        cmbRole.setValue(user.getRole());    
        cmbStatus.setValue(user.getStatus());
    }

    public UserResponse getUpdatedUser() {
        editingUser.setName(txtName.getText());
        editingUser.setUsername(txtUsername.getText());
        editingUser.setRole(cmbRole.getValue());
        editingUser.setStatus(cmbStatus.getValue());
        return editingUser;
    }
}
