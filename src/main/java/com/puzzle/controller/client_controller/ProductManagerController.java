package com.puzzle.controller.client_controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.puzzle.dto.request.CreateProductRequest;
import com.puzzle.dto.response.ProductResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.service.InventoryService;
import com.puzzle.service.ProductService;
import com.puzzle.utils.AlertUtil;
import com.puzzle.utils.SceneManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
public class ProductManagerController {

    // Constants for FXML paths
    private static final String LOGIN_FXML = "/views/GUI/LoginGUI.fxml";
    private static final String KIEM_KE_FXML = "/views/GUI/KiemKeGUI.fxml";
    private static final String DASHBOARD_FXML = "/views/GUI/DashBoardQLKGUI.fxml";
    private static final String YEU_CAU_XUAT_KHO_FXML = "/views/GUI/KhoYCXKGUI.fxml";
    private static final String YEU_CAU_NHAP_KHO_FXML = "/views/GUI/KhoYCNKGUI.fxml";
    private static final String PRODUCT_MANAGER_FXML = "/views/GUI/SanPhamGUI.fxml";

    @FXML private Button logoutButton;
    @FXML private ImageView searchIcon;
    @FXML private Button addProduct;
    @FXML private Label menu_username;
    @FXML private TextField searchProducts;
    @FXML private TableView<ProductResponse> tableView;
    @FXML private TableColumn<ProductResponse, Long> colId;
    @FXML private TableColumn<ProductResponse, String> colName;
    @FXML private TableColumn<ProductResponse, String> colUnit;
    @FXML private TableColumn<ProductResponse, String> colDescription;
    @FXML private TableColumn<ProductResponse, Double> colCostPrice;
    @FXML private TableColumn<ProductResponse, Double> colSellingPrice;
    @FXML private TableColumn<ProductResponse, Void> colUpdate;
    @FXML private TableColumn<ProductResponse, Void> colDelete;

    @Autowired
    private ProductService productService;
    @Autowired
    private InventoryService inventoryService;
    private UserResponse currentUser;

    @FXML
    public void initialize() {
        logoutButton.setOnAction(event -> handleLogout());
        
        if (searchIcon != null) {
            searchIcon.setCursor(Cursor.HAND);
        }
        setupEventHandlers();
        setupTableColumns();
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

    private void handleViewSwitch(String fxmlPath, String viewName) {
        try {
            SceneManager.switchScene(fxmlPath, logoutButton, currentUser);
        } catch (IOException e) {
            AlertUtil.showError("Lỗi khi chuyển đến trang " + viewName + ": " + e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Lỗi không xác định khi chuyển trang: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        addProduct.setOnAction(event -> addProduct());
        searchIcon.setOnMouseClicked(event -> handleSearch());
    }

    private void handleSearch() {
        String pattern = searchProducts.getText();
        List<ProductResponse> searchedProducts = productService.searchProducts(pattern);
        loadDataProducts(searchedProducts != null ? searchedProducts : new ArrayList<>());
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("product_id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCostPrice.setCellValueFactory(new PropertyValueFactory<>("cost_price"));
        colSellingPrice.setCellValueFactory(new PropertyValueFactory<>("selling_price"));

        colUpdate.setCellFactory(getButtonCellFactory("Sửa", "-fx-background-color:rgb(40, 99, 167);", this::updateProduct));
        colDelete.setCellFactory(getButtonCellFactory("Xoá", "-fx-background-color:rgb(255, 0, 0);", this::deleteProduct));
    }

    private Callback<TableColumn<ProductResponse, Void>, TableCell<ProductResponse, Void>> getButtonCellFactory(
            String text, String style, java.util.function.Consumer<ProductResponse> handler) {
        return param -> new TableCell<>() {
            private final Button button = new Button(text);
            {
                button.setStyle(style + " -fx-text-fill: white; -fx-font-size: 10px;");
                button.setOnAction(event -> {
                    ProductResponse product = getTableView().getItems().get(getIndex());
                    if (product != null) {
                        handler.accept(product);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        };
    }

    private void addProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GUI/ThemSanPhamGUI.fxml"));
            DialogPane dialogPane = loader.load();

            ProductAddController controller = loader.getController();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Thêm sản phẩm");

            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.APPLY) {
                    CreateProductRequest newProduct = controller.getAddedProduct();
                    try {
                        productService.createProduct(newProduct);
                        AlertUtil.showInfo("Thêm sản phẩm thành công");
                        updateUI();
                    } catch (Exception e) {
                        AlertUtil.showError(e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi mở cửa sổ thêm sản phẩm");
        }
    }

    private void updateProduct(ProductResponse product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GUI/SuaSanPham.fxml"));
            DialogPane dialogPane = loader.load();

            ProductUpdateController controller = loader.getController();
            controller.setEditingProduct(product);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Sửa sản phẩm");

            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.APPLY) {
                    ProductResponse updated = controller.getUpdatedProduct();
                    try {
                        productService.updateProduct(updated);
                        AlertUtil.showInfo("Cập nhật sản phẩm thành công");
                        updateUI();
                    } catch (Exception e) {
                        AlertUtil.showError("Lỗi khi cập nhật sản phẩm");
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi mở cửa sổ sửa sản phẩm");
        }
    }

    private void deleteProduct(ProductResponse product) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Xóa sản phẩm");
        confirmDialog.setContentText("Bạn có chắc chắn muốn xóa sản phẩm \"" + product.getName() + "\" không?\nHành động này không thể hoàn tác!");
        ButtonType deleteButton = new ButtonType("Xóa", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(deleteButton, cancelButton);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == deleteButton) {
            try {
                productService.deleteProduct(product.getProduct_id());
                removeProductFromTableView(product);
                AlertUtil.showInfo("Xóa sản phẩm thành công!");
            } catch (DataIntegrityViolationException e) {
                AlertUtil.showError("Lỗi khi xóa sản phẩm: " + e.getMessage());
            }
        }
    }

    private void removeProductFromTableView(ProductResponse productToRemove) {
        ObservableList<ProductResponse> items = tableView.getItems();
        items.removeIf(p -> p.getProduct_id().equals(productToRemove.getProduct_id()));
        tableView.refresh();
    }

    public void initData(UserResponse user) {
        this.currentUser = user;
        menu_username.setText(user.getName());
        updateUI();
    }

    public void updateUI() {
        List<ProductResponse> listProducts = productService.getProducts();
        loadDataProducts(listProducts != null ? listProducts : new ArrayList<>());
    }

    public void loadDataProducts(List<ProductResponse> dataProducts) {
        ObservableList<ProductResponse> data = FXCollections.observableArrayList(dataProducts);
        if (Platform.isFxApplicationThread()) {
            tableView.setItems(data);
            tableView.refresh();
        } else {
            Platform.runLater(() -> {
                tableView.setItems(data);
                tableView.refresh();
            });
        }
    }

    // Navigation methods using handleViewSwitch
    @FXML
    public void handleGoToDashBoardProductManager(ActionEvent event) {
        handleViewSwitch(DASHBOARD_FXML, "Dashboard");
    }

    @FXML
    public void handleGoToInventory(ActionEvent event) {
        handleViewSwitch(YEU_CAU_NHAP_KHO_FXML, "Kho YCNK Check");
    }

    @FXML
    private void handleGoToKiemKe(ActionEvent event) {
        handleViewSwitch(KIEM_KE_FXML, "Kiểm kê");
    }

    @FXML
    private void handleGoToYeuCauXuatKho(ActionEvent event) {
        handleViewSwitch(YEU_CAU_XUAT_KHO_FXML, "Yêu cầu xuất kho");
    }

    @FXML
    private void handleYeuCauNhapKho(ActionEvent event) {
        handleViewSwitch(YEU_CAU_NHAP_KHO_FXML, "Yêu cầu nhập kho");
    }
}