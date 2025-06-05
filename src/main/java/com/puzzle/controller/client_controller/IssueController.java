package com.puzzle.controller.client_controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.puzzle.dto.response.StockOutResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.exception.AppException;
import com.puzzle.service.InventoryService;
import com.puzzle.utils.AlertUtil;
import com.puzzle.utils.SceneManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import javafx.stage.Modality;
import javafx.util.Callback;

@Controller
public class IssueController {
    private static final String DASHBOARD_FXML = "/views/GUI/DashBoardNVXKGUI.fxml";
    private static final String ISSUE_FXML = "/views/GUI/XuatKhoGUI.fxml";
    private static final String ISSUE_DETAIL_FXML = "/views/GUI/ChiTietXuatKhoGUI.fxml";
    private static final String FORM_XUATKHO_FXML = "/views/Form/XuatKhoForm.fxml";

    @FXML private Label usernameLbl;
    @FXML private TextField searchField;
    @FXML private Button getDashboardView;
    @FXML private Button getIssueView;
    @FXML private TableView<StockOutResponse> issueTable;
    @FXML private ImageView searchIcon;
    @FXML private Button createStockOut;

    @FXML private TableColumn<StockOutResponse, String> colMaXK;
    @FXML private TableColumn<StockOutResponse, String> colMaNV;
    @FXML private TableColumn<StockOutResponse, String> colNgayTao;
    @FXML private TableColumn<StockOutResponse, String> colNguoiDuyet;
    @FXML private TableColumn<StockOutResponse, String> colNgayDuyet;
    @FXML private TableColumn<StockOutResponse, String> colTrangThai;
    @FXML private TableColumn<StockOutResponse, Void> colChiTiet;

    @Autowired
    private InventoryService inventoryService;
    private UserResponse user;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private boolean isSearched = false;

    private static final String LOGIN_FXML = "/views/GUI/LoginGUI.fxml";
    @FXML private Button logoutButton;


    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận đăng xuất");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            handleViewSwitch(LOGIN_FXML, "Đăng nhập");
        }
    }

    @FXML
    public void initialize() {
        if (searchIcon != null) {
            searchIcon.setCursor(Cursor.HAND);
        }
        
        setupEventHandlers();
        setupTableColumns();
    }

    private void setupEventHandlers() {
        logoutButton.setOnAction(event -> handleLogout());

        if (getDashboardView != null) {
            getDashboardView.setOnAction(event -> handleViewSwitch(DASHBOARD_FXML, "Dashboard"));
        }
        if (getIssueView != null) {
            getIssueView.setOnAction(event -> handleViewSwitch(ISSUE_FXML, "Xuất kho"));
        }
        if (createStockOut != null) {
            createStockOut.setOnAction(event -> handleCreateStockOut());
        }
        if (searchIcon != null) {
            searchIcon.setOnMouseClicked(event -> handleSearch());
        }
    }

    private void setupTableColumns() {
        colMaXK.setCellValueFactory(new PropertyValueFactory<>("request_id"));
        colMaNV.setCellValueFactory(new PropertyValueFactory<>("employee_id"));
        colNgayTao.setCellValueFactory(new PropertyValueFactory<>("created_at"));
        colNguoiDuyet.setCellFactory(new Callback<TableColumn<StockOutResponse, String>, TableCell<StockOutResponse, String>>() {
            @Override
            public TableCell<StockOutResponse, String> call(TableColumn<StockOutResponse, String> param) {
                return new TableCell<StockOutResponse, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null || item.trim().isEmpty()) {
                            setText("Chưa duyệt");
                            setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
                        } else {
                            setText(item);
                            setStyle("");
                        }
                    }
                };
            }
        });
        colNgayDuyet.setCellFactory(new Callback<TableColumn<StockOutResponse,String>,TableCell<StockOutResponse,String>>() {
            public TableCell<StockOutResponse, String> call (TableColumn<StockOutResponse, String> param) {
                return new TableCell<StockOutResponse, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null || item.trim().isEmpty()) {
                            setText("Chưa duyệt");
                            setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
                        } else {
                            setText(item);
                            setStyle("");
                        }
                    }
                };
            }
        });
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("status"));
        colChiTiet.setCellFactory(new Callback<TableColumn<StockOutResponse,Void>,TableCell<StockOutResponse,Void>>() {
            public TableCell<StockOutResponse, Void> call(TableColumn<StockOutResponse, Void> param) {
                return new TableCell<StockOutResponse, Void>() {
                    private final Button detailButton = new Button("Xem");                        
                    {
                        detailButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px;");
                        detailButton.setOnAction(event -> {
                            StockOutResponse stockOut = getTableView().getItems().get(getIndex());
                            if (stockOut != null) {
                                showDetailDialog(stockOut);
                            }
                        });
                    }
                        
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(detailButton);
                        }
                    }
                };
            }
        });
    }

    private void handleCreateStockOut() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FORM_XUATKHO_FXML));
            DialogPane dialogPane = loader.load();
            IssueFormController issueFormController = loader.getController();
            
            if (issueFormController != null) {
                issueFormController.initData(user);
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Tạo Lệnh Xuất Kho");
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (createStockOut != null && createStockOut.getScene() != null) {
                dialog.initOwner(createStockOut.getScene().getWindow());
            }
            dialog.showAndWait();

            if (issueFormController != null && issueFormController.isSubmitted()) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi mở form xuất kho: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi không xác định: " + e.getMessage());
        }
    }

    private void handleSearch() {
        if (searchField == null) return;
        
        String text = searchField.getText();
        if (text == null || text.trim().isEmpty()) {
            if (isSearched) {
                isSearched = false;
            }
            return;
        }
        
        try {
            Long id = Long.parseLong(text.trim());
            System.out.println("Tìm kiếm với id: " + id);
            List<StockOutResponse> issueLists = inventoryService.getStockOutRequestById(id);
            loadIssueData(issueLists);
            isSearched = true;
        } catch (NumberFormatException e) {
                loadIssueData(new ArrayList<>());
                isSearched = true;
                AlertUtil.showError("Vui lòng nhập một số hợp lệ để tìm kiếm.");
        } catch (AppException e) {
                AlertUtil.showError(e.getMessage());
                isSearched = true;
                loadIssueData(new ArrayList<>()); // Truyền empty list thay vì null
        } catch (Exception e) {
                AlertUtil.showError("Lỗi khi tìm kiếm: " + e.getMessage());
                isSearched = true;
                loadIssueData(new ArrayList<>()); // Truyền empty list thay vì null
        }
    }

    private void handleViewSwitch(String fxmlPath, String viewName) {
        try {
            SceneManager.switchScene(fxmlPath, logoutButton, user);
        } catch (IOException e) {
            AlertUtil.showError("Lỗi khi chuyển đến trang " + viewName + ": " + e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Lỗi không xác định khi chuyển trang: " + e.getMessage());
        }
    }

    public void initData(UserResponse user) {
        this.user = user;
        
        if (Platform.isFxApplicationThread()) {
            updateUI();
        } else {
            Platform.runLater(this::updateUI);
        }
    }

    private void updateUI() {
        if (user == null || usernameLbl == null) return;
        
        usernameLbl.setText(user.getUsername());
        List<StockOutResponse> issueList = inventoryService.getStockOutRequests();
        loadIssueData(issueList);
    }


    private void loadIssueData(List<StockOutResponse> issueList) {
        if (issueTable == null) return;
        
        try {
            List<StockOutResponse> safeList = (issueList != null) ? issueList : new ArrayList<>();
            ObservableList<StockOutResponse> data = FXCollections.observableArrayList(safeList);
            
            if (Platform.isFxApplicationThread()) {
                issueTable.setItems(data);
            } else {
                Platform.runLater(() -> issueTable.setItems(data));
            }

            issueTable.refresh();
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi tải dữ liệu xuất kho: " + e.getMessage());
                if (issueTable != null) {
                    issueTable.setItems(FXCollections.observableArrayList());
                    issueTable.refresh();

                }
        }
    }

    private void showDetailDialog(StockOutResponse stockOut) {
        if (stockOut == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ISSUE_DETAIL_FXML));
            DialogPane dialogPane = loader.load();
            
            IssueDetailController detailController = loader.getController();
            if (detailController != null) {
                detailController.initData(stockOut, inventoryService);
            }
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Chi tiết lệnh xuất kho - " + stockOut.getRequest_id());
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (issueTable != null && issueTable.getScene() != null) {
                dialog.initOwner(issueTable.getScene().getWindow());
            }
            
            dialog.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi mở chi tiết: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi không xác định khi mở chi tiết: " + e.getMessage());
        }
    }
}