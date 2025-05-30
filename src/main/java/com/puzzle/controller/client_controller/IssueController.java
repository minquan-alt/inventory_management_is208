package com.puzzle.controller.client_controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.puzzle.dto.response.StockOutResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.exception.AppException;
import com.puzzle.service.InventoryService;
import com.puzzle.utils.AlertUtil;
import com.puzzle.utils.SceneManager;

import org.springframework.stereotype.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
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

    @FXML
    private Label usernameLbl;

    @FXML
    private TextField searchField;

    @FXML
    private Button getDashboardView;

    @FXML
    private Button getIssueView;

    @FXML
    private TableView<StockOutResponse> issueTable;

    @FXML
    private ImageView searchIcon;

    @FXML
    private Button createStockOut;

    @FXML private TableColumn<StockOutResponse, String> colMaXK;
    @FXML private TableColumn<StockOutResponse, String> colMaNV;
    @FXML private TableColumn<StockOutResponse, String> colNgayTao;
    @FXML private TableColumn<StockOutResponse, String> colNguoiDuyet;
    @FXML private TableColumn<StockOutResponse, String> colNgayDuyet;
    @FXML private TableColumn<StockOutResponse, String> colTrangThai;
    @FXML private TableColumn<StockOutResponse, Void> colChiTiet;

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
        getDashboardView.setOnAction(event -> handleViewSwitch(DASHBOARD_FXML, "Dashboard"));
        getIssueView.setOnAction(event -> handleViewSwitch(ISSUE_FXML, "Xuất kho"));

        createStockOut.setOnAction(event -> handleCreateStockOut());
        searchIcon.setOnMouseClicked(event -> handleSearch());
    }
    


    private void setupDetailColumn() {
        colChiTiet.setCellFactory(new Callback<TableColumn<StockOutResponse, Void>, TableCell<StockOutResponse, Void>>() {
            @Override
            public TableCell<StockOutResponse, Void> call(TableColumn<StockOutResponse, Void> param) {
                return new TableCell<StockOutResponse, Void>() {
                    private final Button detailButton = new Button("Xem");
                    
                    {
                        detailButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px;");
                        detailButton.setPrefWidth(60);
                        detailButton.setOnAction(event -> {
                            StockOutResponse stockOut = getTableView().getItems().get(getIndex());
                            showDetailDialog(stockOut);
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
            // load form xuat kho
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FORM_XUATKHO_FXML));
            DialogPane dialogPane = loader.load();
            IssueFormController issueFormController = loader.getController();
            issueFormController.initData(user, inventoryService);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Tạo Lệnh Xuất Kho");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(createStockOut.getScene().getWindow());
            dialog.showAndWait();

            if (issueFormController.isSubmitted()) {
                List<StockOutResponse> issueList = inventoryService.getStockOutRequests();
                loadIssueData(issueList);
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi mở form xuất kho: " + e.getMessage());
        }
    }

    private void handleSearch() {
        String text = searchField.getText();
        if (text == null || text.trim().isEmpty()) {
            if(isSearched) {
                isSearched = false;
                List<StockOutResponse> issueList = inventoryService.getStockOutRequests();
                loadIssueData(issueList);
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
            Platform.runLater(() -> {
                loadIssueData(null);
            });
        } catch (AppException e) {
            Platform.runLater(() -> {
                AlertUtil.showError(e.getMessage());
                loadIssueData(null);
            });
        }
    }

    private void handleViewSwitch(String fxmlPath, String viewName) {
        try {
            SceneManager.switchScene(fxmlPath, getDashboardView, user, inventoryService);
        } catch (IOException e) {
            AlertUtil.showError("Lỗi khi chuyển đến trang " + viewName + ": " + e.getMessage());
        }
    }

    public void initData(UserResponse user, InventoryService inventoryService) {
        this.user = user;
        this.inventoryService = inventoryService;
        
        if (Platform.isFxApplicationThread()) {
            updateUI();
        } else {
            Platform.runLater(() -> {
                updateUI();
            });
        }
    }

    public void updateUI() {
        if (user == null) return;
        usernameLbl.setText(user.getUsername());
        List<StockOutResponse> issueList = inventoryService.getStockOutRequests();
        loadIssueData(issueList);
    }

    private void loadIssueData(List<StockOutResponse> issueList) {
        try {
            ObservableList<StockOutResponse> data = FXCollections.observableArrayList(issueList);
            colMaXK.setCellValueFactory(new PropertyValueFactory<>("request_id"));
            colMaNV.setCellValueFactory(new PropertyValueFactory<>("employee_id"));
            colNgayTao.setCellValueFactory(new PropertyValueFactory<>("created_at"));
            colNguoiDuyet.setCellValueFactory(new PropertyValueFactory<>("approved_by"));
            colNgayDuyet.setCellValueFactory(new PropertyValueFactory<>("approved_at"));
            colTrangThai.setCellValueFactory(new PropertyValueFactory<>("status"));
            
            issueTable.setItems(data);
        } catch (Exception e) {
            AlertUtil.showError("Lỗi khi tải dữ liệu xuất kho: " + e.getMessage());
        }
    }

    

    private void showDetailDialog(StockOutResponse stockOut) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ISSUE_DETAIL_FXML));
            DialogPane dialogPane = loader.load();
            
            IssueDetailController detailController = loader.getController();
            detailController.initData(stockOut, inventoryService);
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Chi tiết lệnh xuất kho - " + stockOut.getRequest_id());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(issueTable.getScene().getWindow());
            
            dialog.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi mở chi tiết: " + e.getMessage());
        }
    }
}
