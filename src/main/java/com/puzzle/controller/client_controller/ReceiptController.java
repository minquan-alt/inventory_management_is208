package com.puzzle.controller.client_controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.puzzle.dto.response.StockInResponse;
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
public class ReceiptController {
    private static final String DASHBOARD_FXML = "/views/GUI/DashBoardNVNKGUI.fxml";
    private static final String RECEIPT_FXML = "/views/GUI/NhapKhoGUI.fxml";

    private static final String RECEIPT_DETAIL_FXML = "/views/GUI/ChiTietNhapKhoGUI.fxml";
    private static final String FORM_NHAPKHO_FXML = "/views/Form/NhapKhoForm.fxml";

    @FXML
    private Label usernameLbl;

    @FXML
    private TextField searchField;

    @FXML
    private Button getDashboardView;

    @FXML
    private Button getReceiptView;

    @FXML
    private TableView<StockInResponse> receiptTable;

    @FXML
    private ImageView searchIcon;

    @FXML
    private Button createStockIn;

    private static final String LOGIN_FXML = "/views/GUI/LoginGUI.fxml";
    @FXML private Button logoutButton;

    @FXML private TableColumn<StockInResponse, String> colMaNK;
    @FXML private TableColumn<StockInResponse, String> colMaNV;
    @FXML private TableColumn<StockInResponse, String> colNgayTao;
    @FXML private TableColumn<StockInResponse, String> colNguoiDuyet;
    @FXML private TableColumn<StockInResponse, String> colNgayDuyet;
    @FXML private TableColumn<StockInResponse, String> colTrangThai;
    @FXML private TableColumn<StockInResponse, Void> colChiTiet;

    @Autowired
    private InventoryService inventoryService;
    private UserResponse user;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    boolean isSearched;

    @FXML
    public void initialize() {
        searchIcon.setCursor(Cursor.HAND);
        isSearched = false;
        setupEventHandlers();
        setupDetailColumn();
    }

    private void setupEventHandlers() {
        logoutButton.setOnAction(event -> handleLogout());
        getDashboardView.setOnAction(event -> handleViewSwitch(DASHBOARD_FXML, "Dashboard"));
        getReceiptView.setOnAction(event -> handleViewSwitch(RECEIPT_FXML, "Nhập kho"));

        createStockIn.setOnAction(event -> handleCreateStockIn());
        searchIcon.setOnMouseClicked(event -> handleSearch());
    }

    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận đăng xuất");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            handleViewSwitch(LOGIN_FXML, "Đăng nhập");
        }
    }


    private void setupDetailColumn() {
        colChiTiet.setCellFactory(new Callback<TableColumn<StockInResponse, Void>, TableCell<StockInResponse, Void>>() {
            @Override
            public TableCell<StockInResponse, Void> call(TableColumn<StockInResponse, Void> param) {
                return new TableCell<StockInResponse, Void>() {
                    private final Button detailButton = new Button("Xem");

                    {
                        detailButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px;");
                        detailButton.setPrefWidth(60);
                        detailButton.setOnAction(event -> {
                            StockInResponse stockIn = getTableView().getItems().get(getIndex());
                            showDetailDialog(stockIn);
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

    private void handleCreateStockIn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FORM_NHAPKHO_FXML));
            DialogPane dialogPane = loader.load();
            ReceiptFormController receiptFormController = loader.getController();
            receiptFormController.initData(user, inventoryService);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Tạo Lệnh Nhập Kho");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(createStockIn.getScene().getWindow());
            dialog.showAndWait();

            if (receiptFormController.isSubmitted()) {
                List<StockInResponse> receiptList = inventoryService.getStockInRequests();
                loadReceiptData(receiptList);
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi mở form nhập kho: " + e.getMessage());
        }
    }

    private void handleSearch() {
        String text = searchField.getText();
        if (text == null || text.trim().isEmpty()) {
            if (isSearched) {
                isSearched = false;
                List<StockInResponse> receiptList = inventoryService.getStockInRequests();
                loadReceiptData(receiptList);
            }
            return;
        }
        try {
            Long id = Long.parseLong(text.trim());
            System.out.println("Tìm kiếm với id: " + id);
            List<StockInResponse> receiptList = inventoryService.getStockInRequestById(id);
            loadReceiptData(receiptList);
            isSearched = true;
        } catch (NumberFormatException e) {
            Platform.runLater(() -> loadReceiptData(null));
        } catch (AppException e) {
            Platform.runLater(() -> {
                AlertUtil.showError(e.getMessage());
                loadReceiptData(null);
            });
        }
    }

    private void handleViewSwitch(String fxmlPath, String viewName) {
        try {
            SceneManager.switchScene(fxmlPath, getDashboardView, user);
        } catch (IOException e) {
            AlertUtil.showError("Lỗi khi chuyển đến trang " + viewName + ": " + e.getMessage());
        }
    }

    public void initData(UserResponse user) {
        this.user = user;

        if (Platform.isFxApplicationThread()) {
            updateUI();
        } else {
            Platform.runLater(() -> updateUI());
        }
    }

    public void updateUI() {
        if (user == null) return;
        usernameLbl.setText(user.getUsername());
        List<StockInResponse> receiptList = inventoryService.getStockInRequests();
        loadReceiptData(receiptList);
    }

    private void loadReceiptData(List<StockInResponse> receiptList) {
        try {
            ObservableList<StockInResponse> data = FXCollections.observableArrayList(receiptList);
            colMaNK.setCellValueFactory(new PropertyValueFactory<>("request_id"));
            colMaNV.setCellValueFactory(new PropertyValueFactory<>("employee_id"));
            colNgayTao.setCellValueFactory(new PropertyValueFactory<>("created_at"));
            colNguoiDuyet.setCellValueFactory(new PropertyValueFactory<>("approved_by"));
            colNgayDuyet.setCellValueFactory(new PropertyValueFactory<>("approved_at"));
            colTrangThai.setCellValueFactory(new PropertyValueFactory<>("status"));

            receiptTable.setItems(data);
        } catch (Exception e) {
            AlertUtil.showError("Lỗi khi tải dữ liệu nhập kho: " + e.getMessage());
        }
    }

    private void showDetailDialog(StockInResponse stockIn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(RECEIPT_DETAIL_FXML));
            DialogPane dialogPane = loader.load();

            ReceiptDetailController detailController = loader.getController();
            detailController.initData(stockIn, inventoryService);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Chi tiết lệnh nhập kho - " + stockIn.getRequest_id());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(receiptTable.getScene().getWindow());

            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi mở chi tiết: " + e.getMessage());
        }
    }
}
