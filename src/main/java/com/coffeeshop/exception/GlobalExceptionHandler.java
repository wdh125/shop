package com.coffeeshop.exception;

import com.coffeeshop.dto.common.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            ex.getMessage(),
            "BadRequest",
            request.getDescription(false),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            400
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            "Không có quyền truy cập vào tài nguyên này",
            "AccessDenied",
            request.getDescription(false),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            403
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String errorMessage = "Dữ liệu đầu vào không hợp lệ: " + errors.toString();
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            errorMessage,
            "ValidationError",
            request.getDescription(false),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            400
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(
            Exception ex, WebRequest request) {
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            "Đã xảy ra lỗi nội bộ. Vui lòng thử lại sau.",
            "InternalServerError",
            request.getDescription(false),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            500
        );
        
        // Log the actual exception for debugging (but don't expose it to client)
        System.err.println("Internal Server Error: " + ex.getMessage());
        ex.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
