package com.coffeeshop.dto.common;

public class MessageResponseDTO {
    private String message;
    private String type;
    private String timestamp;

    public MessageResponseDTO() {}
    
    public MessageResponseDTO(String message) { 
        this.message = message; 
    }
    
    public MessageResponseDTO(String message, String type) {
        this.message = message;
        this.type = type;
    }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
} 