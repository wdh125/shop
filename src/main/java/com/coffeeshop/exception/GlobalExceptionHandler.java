package com.coffeeshop.exception;

import com.coffeeshop.dto.common.ErrorResponseDTO;
import com.coffeeshop.dto.common.ValidationErrorResponseDTO;
import com.coffeeshop.dto.common.FieldErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");

    // Business Logic Exceptions
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        
        logger.warn("User not found: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("errorCode", ex.getErrorCode());
        if (ex.getRejectedValue() != null) {
            details.put("rejectedValue", ex.getRejectedValue());
        }
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            ex.getMessage(),
            "Not Found",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            404,
            details
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, WebRequest request) {
        
        logger.warn("User already exists: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("errorCode", ex.getErrorCode());
        if (ex.getRejectedValue() != null) {
            details.put("rejectedValue", ex.getRejectedValue());
        }
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            ex.getMessage(),
            "Conflict",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            409,
            details
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentialsException(
            InvalidCredentialsException ex, WebRequest request) {
        
        logger.warn("Invalid credentials: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("errorCode", ex.getErrorCode());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            ex.getMessage(),
            "Unauthorized",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            401,
            details
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler({OrderNotFoundException.class, ProductNotFoundException.class, ReservationNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            BusinessLogicException ex, WebRequest request) {
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("errorCode", ex.getErrorCode());
        if (ex.getRejectedValue() != null) {
            details.put("rejectedValue", ex.getRejectedValue());
        }
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            ex.getMessage(),
            "Not Found",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            404,
            details
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedAccessException(
            UnauthorizedAccessException ex, WebRequest request) {
        
        logger.warn("Unauthorized access: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("errorCode", ex.getErrorCode());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            ex.getMessage(),
            "Forbidden",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            403,
            details
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("errorCode", ex.getErrorCode());
        if (ex.getRejectedValue() != null) {
            details.put("rejectedValue", ex.getRejectedValue());
        }
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            ex.getMessage(),
            "Bad Request",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            400,
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Generic Business Logic Exception Handler
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessLogicException(
            BusinessLogicException ex, WebRequest request) {
        
        logger.warn("Business logic error: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getErrorCode() != null) {
            details.put("errorCode", ex.getErrorCode());
        }
        if (ex.getRejectedValue() != null) {
            details.put("rejectedValue", ex.getRejectedValue());
        }
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            ex.getMessage(),
            "Bad Request",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            400,
            details.isEmpty() ? null : details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Existing handlers with improvements
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            ex.getMessage(),
            "Bad Request",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            400
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        logger.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            "Không có quyền truy cập vào tài nguyên này",
            "Forbidden",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            403
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Validation failed for request: {}", extractPath(request));
        
        List<FieldErrorDTO> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            fieldErrors.add(new FieldErrorDTO(fieldName, rejectedValue, errorMessage));
        });
        
        String errorMessage = "Dữ liệu đầu vào không hợp lệ";
        
        ValidationErrorResponseDTO errorResponse = new ValidationErrorResponseDTO(
            errorMessage,
            "Bad Request",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            400,
            fieldErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        logger.warn("Method argument type mismatch: {}", ex.getMessage());
        
        String message = "Giá trị không hợp lệ";
        Map<String, Object> details = new HashMap<>();
        details.put("parameterName", ex.getName());
        details.put("rejectedValue", ex.getValue());
        
        // Special handling for enum conversion errors
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) ex.getRequiredType();
            String[] validValues = java.util.Arrays.stream(enumClass.getEnumConstants())
                    .map(Enum::name)
                    .toArray(String[]::new);
            message = "Giá trị '" + ex.getValue() + "' không hợp lệ cho tham số '" + ex.getName() + 
                     "'. Các giá trị hợp lệ: " + String.join(", ", validValues);
            details.put("validValues", validValues);
        }
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            message,
            "Bad Request",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            400,
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(
            Exception ex, WebRequest request) {
        
        // Log chi tiết lỗi để debug
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            "Đã xảy ra lỗi nội bộ. Vui lòng thử lại sau.",
            "Internal Server Error",
            extractPath(request),
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            500
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        if (description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description;
    }
}
