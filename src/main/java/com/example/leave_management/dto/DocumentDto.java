package com.example.leave_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DocumentDto {
    @NotBlank(message = "Document type is required")
    private String type; // "image" or "file"

    @NotBlank(message = "File path is required")
    private String filePath;

    public DocumentDto() {
    }

    public DocumentDto(String type, String filePath) {
        this.type = type;
        this.filePath = filePath;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}