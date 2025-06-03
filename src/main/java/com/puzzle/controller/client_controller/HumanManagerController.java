package com.puzzle.controller.client_controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.puzzle.dto.request.CreateUserRequest;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.exception.AppException;
import com.puzzle.service.UserService;
import com.puzzle.utils.AlertUtil;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

@Controller
public class HumanManagerController {
    @FXML private ImageView searchIcon;
    @FXML private Button addUser;
    @FXML private Label menu_username;
    @FXML private TextField searchUsers;
    @FXML private TableView<UserResponse> tableView;
    @FXML private TableColumn<UserResponse, String> colId;
    @FXML private TableColumn<UserResponse, String> colName;
    @FXML private TableColumn<UserResponse, String> colUsername;
    @FXML private TableColumn<UserResponse, String> colRole;
    @FXML private TableColumn<UserResponse, String> colStatus;
    @FXML private TableColumn<UserResponse, Void> colUpdate;
    @FXML private TableColumn<UserResponse, Void> colDelete;


    private UserService userService;
    private UserResponse user;


    @FXML
    public void initialize() {
        if(searchIcon != null) {
            searchIcon.setCursor(Cursor.HAND);
        }
        setupEventHandlers();
        setupTableColumns();
    }

    private void setupEventHandlers() {
        addUser.setOnAction(event -> addUser());
        searchIcon.setOnMouseClicked(event -> handleSearch());
    }

    private void handleSearch() {
        String pattern = searchUsers.getText();
        List<UserResponse> searchedUsers = userService.searchUsers(pattern);
        if(searchedUsers == null) {
            loadDataUsers(new ArrayList<>());
        } else {
            loadDataUsers(searchedUsers);
        }

    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colUpdate.setCellFactory(new Callback<TableColumn<UserResponse,Void>,TableCell<UserResponse,Void>>() {
            @Override
            public TableCell<UserResponse, Void> call(TableColumn<UserResponse, Void> param) {
                return new TableCell<UserResponse, Void>() {
                    private final Button updateButton = new Button("Sửa");
                    {
                        updateButton.setStyle("-fx-background-color:rgb(40, 99, 167); -fx-text-fill: white; -fx-font-size: 10px;");
                        updateButton.setOnAction(event -> {
                            UserResponse user = getTableView().getItems().get(getIndex());
                            if (user != null) {
                                updateUser(user);
                            }
                        });
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(updateButton);
                        }
                    }
                };
            }
        });

        colDelete.setCellFactory(new Callback<TableColumn<UserResponse,Void>,TableCell<UserResponse,Void>>() {
            @Override
            public TableCell<UserResponse, Void> call(TableColumn<UserResponse, Void> param) {
                return new TableCell<UserResponse, Void>() {
                    private final Button deleteButton = new Button("Xoá");
                    {
                        deleteButton.setStyle("-fx-background-color:rgb(255, 0, 0); -fx-text-fill: white; -fx-font-size: 10px;");
                        deleteButton.setOnAction(event -> {
                            UserResponse user = getTableView().getItems().get(getIndex());
                            if (user != null) {
                                deleteUser(user);
                            }
                        });
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(deleteButton);
                        }
                    }
                };
            }
        });
    }

    private void addUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GUI/ThemNguoiDungGUI.fxml"));
            DialogPane dialogPane = loader.load();

            HumanAddController controller = loader.getController();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Thêm người dùng");

            dialog.showAndWait().ifPresent(buttonType -> {
                if(buttonType.getButtonData() == ButtonBar.ButtonData.APPLY) {
                    CreateUserRequest addedUser = controller.getAddedUser();
                    try {
                        UserResponse newUser = userService.createUser(addedUser);
                        AlertUtil.showInfo("Thêm người dùng thành công");
                        updateUI();
                    } catch (AppException e) {
                        AlertUtil.showError(e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi mở cửa sổ thêm");
        }
    }

    private void updateUser(UserResponse user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GUI/SuaNguoiDungGUI.fxml"));
            DialogPane dialogPane = loader.load();

            HumanUpdateController dialogController = loader.getController();
            dialogController.setEditingUser(user);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Cập nhật người dùng");

            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.APPLY) {
                    UserResponse updated = dialogController.getUpdatedUser();
                    try {
                        UserResponse newUser = userService.updateUser(updated);
                        System.out.print("Update user successfully");
                        updateUI();
                        AlertUtil.showInfo("Cập nhật người dùng thành công");
                    } catch (Exception ex) {
                        AlertUtil.showError("Lỗi khi cập nhật người dùng");
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi mở cửa sổ cập nhật");
        }
    }


    private void deleteUser(UserResponse user) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Xóa người dùng");
        confirmDialog.setContentText("Bạn có chắc chắn muốn xóa người dùng \"" + user.getName() + "\" không?\n" +
                                    "Hành động này không thể hoàn tác!");
        ButtonType deleteButton = new ButtonType("Xóa", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(deleteButton, cancelButton);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if(result.isPresent() && result.get() == deleteButton) {
            try {
                userService.deleteUser(user.getId());
                removeUserFromTableView(user);
                AlertUtil.showInfo("Xóa người dùng thành công!");
            } catch (DataIntegrityViolationException e) {
                AlertUtil.showError("Lỗi khi xóa người dùng: " + e.getMessage());
            }
        }
    }

    private void removeUserFromTableView(UserResponse userToRemove) {
        if (tableView != null && tableView.getItems() != null) {
            ObservableList<UserResponse> items = tableView.getItems();
            items.removeIf(user -> user.getId().equals(userToRemove.getId()));
            tableView.refresh();
        }
    }

    public void initData(UserService userService, UserResponse userResponse) {
        this.user = userResponse;
        this.userService = userService;

        if (Platform.isFxApplicationThread()) {
            updateUI();
        } else {
            Platform.runLater(() -> {
                updateUI();
            });
        }
    }

    public void updateUI() {
        List<UserResponse> listUsers = userService.getUsers();
        if(listUsers.isEmpty()) {
            loadDataUsers(null);
        } else {
            loadDataUsers(listUsers);
        }
    }

    public void loadDataUsers(List<UserResponse> dataUsers) {
        if(dataUsers == null) {
            dataUsers = new ArrayList<>();
        }

        try {
            ObservableList<UserResponse> data = FXCollections.observableArrayList(dataUsers);

            if (Platform.isFxApplicationThread()) {
                tableView.setItems(data);
                tableView.refresh();
            } else {
                Platform.runLater(() ->{
                    tableView.setItems(data);
                    tableView.refresh();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi tải dữ liệu xuất kho: " + e.getMessage());
            Platform.runLater(() -> {
                if (tableView != null) {
                    tableView.setItems(FXCollections.observableArrayList());
                    tableView.refresh();
                }
            });
        }
    }
}
