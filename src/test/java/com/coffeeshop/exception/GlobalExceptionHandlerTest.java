package com.coffeeshop.exception;

import com.coffeeshop.dto.common.ErrorResponseDTO;
import com.coffeeshop.dto.common.ValidationErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for GlobalExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("Handle UserNotFoundException - Success")
    void handleUserNotFoundException_Success() {
        // Given
        UserNotFoundException exception = new UserNotFoundException("User not found");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleUserNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("User not found");
        assertThat(response.getBody().getStatus()).isEqualTo("Not Found");
        assertThat(response.getBody().getStatusCode()).isEqualTo(404);
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Handle OrderNotFoundException - Success")
    void handleOrderNotFoundException_Success() {
        // Given
        OrderNotFoundException exception = new OrderNotFoundException("Order not found");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleOrderNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Order not found");
        assertThat(response.getBody().getStatus()).isEqualTo("Not Found");
        assertThat(response.getBody().getStatusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Handle ProductNotFoundException - Success")
    void handleProductNotFoundException_Success() {
        // Given
        ProductNotFoundException exception = new ProductNotFoundException("Product not found");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleProductNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Product not found");
        assertThat(response.getBody().getStatus()).isEqualTo("Not Found");
        assertThat(response.getBody().getStatusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Handle ReservationNotFoundException - Success")
    void handleReservationNotFoundException_Success() {
        // Given
        ReservationNotFoundException exception = new ReservationNotFoundException("Reservation not found");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleReservationNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Reservation not found");
        assertThat(response.getBody().getStatus()).isEqualTo("Not Found");
        assertThat(response.getBody().getStatusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Handle UserAlreadyExistsException - Success")
    void handleUserAlreadyExistsException_Success() {
        // Given
        UserAlreadyExistsException exception = new UserAlreadyExistsException("User already exists");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleUserAlreadyExistsException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("User already exists");
        assertThat(response.getBody().getStatus()).isEqualTo("Conflict");
        assertThat(response.getBody().getStatusCode()).isEqualTo(409);
    }

    @Test
    @DisplayName("Handle InvalidCredentialsException - Success")
    void handleInvalidCredentialsException_Success() {
        // Given
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleInvalidCredentialsException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
        assertThat(response.getBody().getStatus()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Handle UnauthorizedAccessException - Success")
    void handleUnauthorizedAccessException_Success() {
        // Given
        UnauthorizedAccessException exception = new UnauthorizedAccessException("Unauthorized access");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleUnauthorizedAccessException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Unauthorized access");
        assertThat(response.getBody().getStatus()).isEqualTo("Forbidden");
        assertThat(response.getBody().getStatusCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("Handle BusinessLogicException - Success")
    void handleBusinessLogicException_Success() {
        // Given
        BusinessLogicException exception = new BusinessLogicException("Business logic error");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleBusinessLogicException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Business logic error");
        assertThat(response.getBody().getStatus()).isEqualTo("Bad Request");
        assertThat(response.getBody().getStatusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Handle ValidationException - Success")
    void handleValidationException_Success() {
        // Given
        ValidationException exception = new ValidationException("Validation error");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation error");
        assertThat(response.getBody().getStatus()).isEqualTo("Bad Request");
        assertThat(response.getBody().getStatusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Handle MethodArgumentNotValidException - Success")
    void handleMethodArgumentNotValidException_Success() {
        // Given
        FieldError fieldError1 = new FieldError("testObject", "field1", "Field1 is required");
        FieldError fieldError2 = new FieldError("testObject", "field2", "Field2 must be valid");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ValidationErrorResponseDTO> response = globalExceptionHandler.handleMethodArgumentNotValidException(
                methodArgumentNotValidException, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getStatus()).isEqualTo("Bad Request");
        assertThat(response.getBody().getStatusCode()).isEqualTo(400);
        assertThat(response.getBody().getFieldErrors()).hasSize(2);
        assertThat(response.getBody().getFieldErrors().get(0).getField()).isEqualTo("field1");
        assertThat(response.getBody().getFieldErrors().get(0).getMessage()).isEqualTo("Field1 is required");
        assertThat(response.getBody().getFieldErrors().get(1).getField()).isEqualTo("field2");
        assertThat(response.getBody().getFieldErrors().get(1).getMessage()).isEqualTo("Field2 must be valid");
    }

    @Test
    @DisplayName("Handle AccessDeniedException - Success")
    void handleAccessDeniedException_Success() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
        assertThat(response.getBody().getStatus()).isEqualTo("Forbidden");
        assertThat(response.getBody().getStatusCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("Handle Generic Exception - Success")
    void handleGenericException_Success() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getStatus()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getStatusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("Handle IllegalArgumentException - Success")
    void handleIllegalArgumentException_Success() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid argument");
        assertThat(response.getBody().getStatus()).isEqualTo("Bad Request");
        assertThat(response.getBody().getStatusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Handle NullPointerException - Success")
    void handleNullPointerException_Success() {
        // Given
        NullPointerException exception = new NullPointerException("Null pointer");

        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleNullPointerException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected null pointer error occurred");
        assertThat(response.getBody().getStatus()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getStatusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("Extract path from WebRequest - Success")
    void extractPath_Success() {
        // Given
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users/123");

        // When - This tests the private method indirectly through exception handling
        UserNotFoundException exception = new UserNotFoundException("User not found");
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleUserNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getBody().getPath()).isEqualTo("/api/users/123");
    }

    @Test
    @DisplayName("Extract path from WebRequest - No URI")
    void extractPath_NoURI() {
        // Given
        when(webRequest.getDescription(false)).thenReturn("other description");

        // When
        UserNotFoundException exception = new UserNotFoundException("User not found");
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleUserNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getBody().getPath()).isEqualTo("Unknown");
    }
}