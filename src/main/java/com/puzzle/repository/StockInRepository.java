package com.puzzle.repository;

import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class StockInRepository {

    private final EntityManager entityManager;

    public Map<String, Object> createStockInRequest(Long employeeId, String productJson) {
        try {
            StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("create_stock_in_request")
                .registerStoredProcedureParameter(1, Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter(2, String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(3, Long.class, ParameterMode.OUT)
                .registerStoredProcedureParameter(4, Boolean.class, ParameterMode.OUT)
                .registerStoredProcedureParameter(5, String.class, ParameterMode.OUT)
                .setParameter(1, employeeId)
                .setParameter(2, productJson);

            query.execute();

            Map<String, Object> result = new HashMap<>();
            result.put("requestId", query.getOutputParameterValue(3));
            result.put("success", query.getOutputParameterValue(4));
            result.put("message", query.getOutputParameterValue(5));

            return result;
        } catch (Exception e) {
            System.out.println("Failed to execute stored procedure: " + e.getMessage());
            throw new AppException(ErrorCode.CAN_NOT_EXECUTE_STORE_PROCEDURE);
        }
    }
}
