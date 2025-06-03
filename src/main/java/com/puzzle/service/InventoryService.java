package com.puzzle.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puzzle.dto.request.InventoryCheckDetailRequest;
import com.puzzle.dto.request.InventoryCheckRequest;
import com.puzzle.dto.request.ProductRequest;
import com.puzzle.dto.request.ProductRequestIn;
import com.puzzle.dto.request.StockInRequest;
import com.puzzle.dto.request.StockOutRequest;
import com.puzzle.dto.response.InventoryCheckDetailResponse;
import com.puzzle.dto.response.InventoryCheckResponse;
import com.puzzle.dto.response.InventoryResponse;
import com.puzzle.dto.response.StockInDetailsResponse;
import com.puzzle.dto.response.StockInResponse;
import com.puzzle.dto.response.StockOutDetailsResponse;
import com.puzzle.dto.response.StockOutResponse;
import com.puzzle.entity.Inventory;
import com.puzzle.entity.InventoryCheck;
import com.puzzle.entity.InventoryCheckDetail;
import com.puzzle.entity.InventoryCheckDetailKey;
import com.puzzle.entity.InventoryLog;
import com.puzzle.entity.Product;
import com.puzzle.entity.StockRequestDetails;
import com.puzzle.entity.StockRequests;
import com.puzzle.entity.StockRequests.RequestType;
import com.puzzle.entity.StockRequests.Status;
import com.puzzle.entity.User;
import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import com.puzzle.mapper.InventoryCheckMapper;
import com.puzzle.repository.InventoryCheckRepository;
import com.puzzle.repository.InventoryLogRepository;
import com.puzzle.repository.InventoryRepository;
import com.puzzle.repository.ProductRepository;
import com.puzzle.repository.StockInRepository;
import com.puzzle.repository.StockOutRepository;
import com.puzzle.repository.StockRequestDetailsRepository;
import com.puzzle.repository.StockRequestsRepository;
import com.puzzle.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    @Autowired
    private StockOutRepository stockOutRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockRequestsRepository stockRequestsRepository;

    @Autowired
    private StockRequestDetailsRepository stockRequestDetailsRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    @Autowired
    private InventoryCheckRepository inventoryCheckRepository;

    @Autowired
    private InventoryCheckMapper inventoryCheckMapper;

    @Autowired
    private StockInRepository stockInRepository;




    public StockOutDetailsResponse mapToStockOutRequestDetailsResponse(StockRequestDetails stockRequestDetails) {
        return StockOutDetailsResponse.builder()
            .id(stockRequestDetails.getId())
            .request_id(stockRequestDetails.getStockRequests().getId())
            .product_id(stockRequestDetails.getProduct().getProduct_id())
            .product_name(stockRequestDetails.getProduct().getName())
            .unit(stockRequestDetails.getProduct().getUnit())
            .quantity(stockRequestDetails.getQuantity())
            .unit_cost(stockRequestDetails.getUnitPrice())
            .build();
    }

    public StockOutResponse mapToStockRequestsResponse(StockRequests stockRequests) {
        return StockOutResponse.builder()
            .request_id(stockRequests.getId())
            .request_type(stockRequests.getRequestType().toString())
            .status(stockRequests.getStatus().toString())
            .employee_id(stockRequests.getUser().getId())
            .created_at(stockRequests.getCreatedAt())
            .approved_at(stockRequests.getApprovedAt())
            .approved_by(stockRequests.getApprovedBy() != null ? stockRequests.getApprovedBy().getId() : null)
            .build();
    }

    public InventoryResponse mapToInventoryResponse(Inventory inventory) {
        return InventoryResponse.builder()
            .inventory_id(inventory.getInventory_id())
            .product_id(inventory.getProduct().getProduct_id())
            .product_name(inventory.getProduct().getName())
            .quantity(inventory.getQuantity())
            .build();
    }


    public List<InventoryResponse> getInventory() {
        List<Inventory> results = inventoryRepository.findAll();
        return results
            .stream()
            .map(this::mapToInventoryResponse)
            .collect(Collectors.toList());
    }

    public List<StockOutDetailsResponse> getStockOutDetailsResponses(Long stockRequestId) {
        List<StockRequestDetails> results = stockRequestDetailsRepository.findByStockRequests_Id(stockRequestId); 
        return results
            .stream()
            .map(this::mapToStockOutRequestDetailsResponse)
            .collect(Collectors.toList());
    }

    public List<StockOutResponse> getStockOutRequests() {
        List<StockRequests> results = stockRequestsRepository.findByRequestType(RequestType.OUT)
            .orElseThrow(() -> {
                throw new AppException(ErrorCode.STOCK_OUT_REQUEST_NOT_FOUND);
            });
        
        return results
                .stream()
                .map(this::mapToStockRequestsResponse)
                .collect(Collectors.toList());
    }



    public List<StockOutResponse> getStockOutRequestById(Long id) {
        List<StockRequests> results = stockRequestsRepository.findByIdAndRequestType(id ,RequestType.OUT)
            .orElseThrow(() -> {
                throw new AppException(ErrorCode.STOCK_OUT_REQUEST_NOT_FOUND);
            });
        
        return results
                .stream()
                .map(this::mapToStockRequestsResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> createStockOut(StockOutRequest request, long employee_id) throws JsonProcessingException{
        List<Long> productIds = new ArrayList<>();
        for (ProductRequest productRequest : request.getProducts()) {
            productIds.add(productRequest.getProduct_id());
        }
        try {
            productService.checkProductsByIds(productIds);
        } catch (AppException e) {
            throw e;
        }

        try {
            userService.getUser(employee_id);
        } catch (AppException e) {
            throw e;
        }

        String productJson = objectMapper.writeValueAsString(request.getProducts());
        return stockOutRepository.createStockOutRequest(employee_id, productJson);
    }

    public StockOutResponse approveStockOutRequest(long stock_request_id, HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        if(managerId == null) {
            throw new AppException(ErrorCode.NOT_AUTHENTICATED);
        }
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> {
                return new AppException(ErrorCode.USER_NOT_FOUND);
            });
        if (manager.getRole().toString() != "ROLE_PRODUCT_MANAGEMENT") {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        StockRequests stockRequest = stockRequestsRepository.findById(stock_request_id)
            .orElseThrow(() -> {
                throw new AppException(ErrorCode.STOCK_OUT_REQUEST_NOT_FOUND);
            });
        
        stockRequest.setStatus(Status.APPROVED);
        stockRequest.setApprovedAt(LocalDateTime.now());
        stockRequest.setApprovedBy(manager);

        stockRequestsRepository.save(stockRequest);

        return mapToStockRequestsResponse(stockRequest);
    }

    public String declinedStockOutRequest(long stock_request_id, HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        if(managerId == null) {
            throw new AppException(ErrorCode.NOT_AUTHENTICATED);
        }
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> {
                return new AppException(ErrorCode.USER_NOT_FOUND);
            });
        if (manager.getRole().toString() != "ROLE_PRODUCT_MANAGEMENT") {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        StockRequests stockRequest = stockRequestsRepository.findById(stock_request_id)
            .orElseThrow(() -> {
                throw new AppException(ErrorCode.STOCK_OUT_REQUEST_NOT_FOUND);
            });
        
        stockRequest.setStatus(Status.DECLINED);
        stockRequest.setApprovedAt(LocalDateTime.now());
        stockRequest.setApprovedBy(manager);

        stockRequestsRepository.save(stockRequest);
        
        return "Declined successfull";
    }

    public StockInDetailsResponse mapToStockInRequestDetailsResponse(StockRequestDetails details) {
        return StockInDetailsResponse.builder()
            .id(details.getId())
            .request_id(details.getStockRequests().getId())
            .product_id(details.getProduct().getProduct_id())
            .quantity(details.getQuantity())
            .unit_cost(details.getUnitPrice())
            .build();
    }

        public StockInResponse mapToStockInRequestsResponse(StockRequests stockRequests) {
        return StockInResponse.builder()
            .request_id(stockRequests.getId())
            .request_type(stockRequests.getRequestType().toString())
            .status(stockRequests.getStatus().toString())
            .employee_id(stockRequests.getUser().getId())
            .created_at(stockRequests.getCreatedAt())
            .approved_at(stockRequests.getApprovedAt())
            .approved_by(stockRequests.getApprovedBy() != null ? stockRequests.getApprovedBy().getId() : null)
            .build();
    }
    
    public List<StockInDetailsResponse> getStockInDetailsResponses(Long stockRequestId) {
        List<StockRequestDetails> results = stockRequestDetailsRepository.findByStockRequests_Id(stockRequestId);
        return results.stream()
            .map(this::mapToStockInRequestDetailsResponse)
            .collect(Collectors.toList());
    }

    public List<StockInResponse> getStockInRequests(HttpSession session) {
        List<StockRequests> results = stockRequestsRepository.findByRequestType(RequestType.IN)
            .orElseThrow(() -> new AppException(ErrorCode.STOCK_IN_REQUEST_NOT_FOUND));
        return results.stream()
            .map(this::mapToStockInRequestsResponse)
            .collect(Collectors.toList());
    }

    public Map<String, Object> createStockIn(StockInRequest request, long employee_id) throws JsonProcessingException {
        List<Long> productIds = new ArrayList<>();
        for (ProductRequestIn productRequest : request.getProducts()) {
            productIds.add(productRequest.getProduct_id());
        }

        productService.checkProductsByIds(productIds);

        userService.getUser(employee_id);

        try {
            String productJson = objectMapper.writeValueAsString(request.getProducts());
            System.out.println();
            return stockInRepository.createStockInRequest(employee_id, productJson);
        } catch (Exception e) {
            throw new AppException(ErrorCode.ERROR_IN_CREATE_STOCK_IN_REQUEST_PROCESS);
        }
    }

     public StockInResponse approveStockInRequest(long stock_request_id, HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        if(managerId == null) {
            throw new AppException(ErrorCode.NOT_AUTHENTICATED);
        }
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!"ROLE_PRODUCT_MANAGEMENT".equals(manager.getRole().toString())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        StockRequests stockRequest = stockRequestsRepository.findById(stock_request_id)
            .orElseThrow(() -> new AppException(ErrorCode.STOCK_IN_REQUEST_NOT_FOUND));

        stockRequest.setStatus(Status.APPROVED);
        stockRequest.setApprovedAt(LocalDateTime.now());
        stockRequest.setApprovedBy(manager);

        stockRequestsRepository.save(stockRequest);

        return mapToStockInRequestsResponse(stockRequest);
    }

    public String declinedStockInRequest(long stock_request_id, HttpSession session) {
        Long managerId = (Long) session.getAttribute("userId");
        if(managerId == null) {
            throw new AppException(ErrorCode.NOT_AUTHENTICATED);
        }
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!"ROLE_PRODUCT_MANAGEMENT".equals(manager.getRole().toString())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        StockRequests stockRequest = stockRequestsRepository.findById(stock_request_id)
            .orElseThrow(() -> new AppException(ErrorCode.STOCK_IN_REQUEST_NOT_FOUND));

        stockRequest.setStatus(Status.DECLINED);
        stockRequest.setApprovedAt(LocalDateTime.now());
        stockRequest.setApprovedBy(manager);

        stockRequestsRepository.save(stockRequest);
        return "Declined successfully";
    }

    public InventoryCheckResponse createCheck(Long userId, InventoryCheckRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        //Check user ton tai hay k
        if (!(user.getRole() == User.Role.ROLE_PRODUCT_MANAGEMENT ||
                user.getRole() == User.Role.ROLE_RECEIPT)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        //Check product ton tai hay k
        List<Long> productIds = request.getInventoryCheckDetailRequests()
                .stream().map(InventoryCheckDetailRequest::getProductId).toList();

        List<Product> products = productRepository.findAllById(productIds);

        if (products.size() != productIds.size()){
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProduct_id, p -> p));


        InventoryCheck inventoryCheck = InventoryCheck.builder()
                .createdBy(user)
                .note(request.getNote() != null ? request.getNote() : "No Note")
                .build();

        inventoryCheckRepository.save(inventoryCheck);

        List<InventoryCheckDetail> detailInventoryCheckDetails = new ArrayList<>();

        for(InventoryCheckDetailRequest rq : request.getInventoryCheckDetailRequests()){
            Long productId = rq.getProductId();
            Product product = productMap.get(productId);

            int systemQuantity = inventoryRepository.findByProductId(productId)
                    .map(Inventory::getQuantity)
                    .orElse(0);

            InventoryCheckDetail detail = InventoryCheckDetail.builder()
                    .id(new InventoryCheckDetailKey(inventoryCheck.getCheck_id(), productId))
                    .inventoryCheck(inventoryCheck)
                    .product(product)
                    .actualQuantity(rq.getActualQuantity())
                    .systemQuantity(systemQuantity)
                    .note(rq.getNote() != null ? rq.getNote() : "No Note")
                    .build();
//            inventoryCheckDetailRepository.save(detail);

            detailInventoryCheckDetails.add(detail);

            int adjustment = rq.getActualQuantity() - systemQuantity;
            if (adjustment != 0) {
                InventoryLog log = InventoryLog.builder()
                        .product(product)
                        .changeType(InventoryLog.ChangeType.ADJUST)
                        .quantityChange(adjustment)
                        .referenceId(inventoryCheck.getCheck_id())
                        .referenceType(InventoryLog.ReferenceType.COUNT)
                        .createdBy(user)
                        .build();
                inventoryLogRepository.save(log);
            }
        }


        List<InventoryCheckDetailResponse> DetailResponseList = inventoryCheckMapper.toDetailResponseList(detailInventoryCheckDetails);

        inventoryCheck.setDetails(detailInventoryCheckDetails);
        inventoryCheckRepository.save(inventoryCheck);

        return inventoryCheckMapper.toCheckResponse(inventoryCheck);

    }

    @Transactional
    public List<InventoryCheckResponse> getAllInventoryCheck(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!(user.getRole() == User.Role.ROLE_PRODUCT_MANAGEMENT ||
                user.getRole() == User.Role.ROLE_RECEIPT)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        return inventoryCheckMapper.toListCheckResponse(inventoryCheckRepository.findAll());
    }


    public InventoryCheckResponse getInventoryCheck(Long userId, Long inventoryCheckId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!(user.getRole() == User.Role.ROLE_PRODUCT_MANAGEMENT ||
                user.getRole() == User.Role.ROLE_RECEIPT)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        InventoryCheck check = inventoryCheckRepository.findById(inventoryCheckId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        return inventoryCheckMapper.toCheckResponse(check);    
    }

    
}
