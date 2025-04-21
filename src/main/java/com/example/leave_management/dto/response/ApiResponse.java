package com.example.leave_management.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ApiResponse<T> {
    private String message;
    private boolean success;
    private HttpStatus status = HttpStatus.OK;
    private String date;

    @JsonIgnore
    private T data;

    @JsonIgnore
    private Map<String, Object> dynamicData = new HashMap<>();

    public ApiResponse(String message, T data, boolean success, HttpStatus status, String type) {
        this.message = message;
        this.data = data;
        this.success = success;
        this.status = status;
        this.date = LocalDateTime.now().toString();
        dynamicData.put(type != null && !type.isEmpty() ? type : "data", data);
    }

    public static <T> ApiResponse<T> createResponse(
            String message,
            T data,
            boolean success,
            HttpStatus status,
            HttpServletRequest request,
            String type) {
        ApiResponse<T> response = new ApiResponse<>(message, data, success, status, type);
        return response;
    }

    public void setDataWithKey(String key, T data) {
        dynamicData.clear(); // Clear previous data
        dynamicData.put(key, data);
    }

    @JsonAnyGetter
    public Map<String, Object> getDynamicData() {
        return dynamicData;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "ResponseDto{" +
                "message='" + message + '\'' +
                ", data=" + data +
                ", success=" + success +
                ", status=" + status +
                ", date='" + date + '\'' +
                '}';
    }
}
