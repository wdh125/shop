package com.coffeeshop.exception;

import com.coffeeshop.dto.common.ErrorResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the GlobalExceptionHandler to verify custom exception handling.
 * This test can run without database connection.
 */
@SpringBootTest
class ExceptionHandlingTest {

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void testUserNotFoundException() {
        // Given
        UserNotFoundException exception = new UserNotFoundException("User not found", "testuser");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/testuser");
        WebRequest webRequest = new ServletRequestAttributes(request);
        
        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleUserNotFoundException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        ErrorResponseDTO errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("User not found", errorResponse.getMessage());
        assertEquals("Not Found", errorResponse.getError());
        assertEquals(404, errorResponse.getStatus());
        assertNotNull(errorResponse.getDetails());
        assertEquals("USER_NOT_FOUND", errorResponse.getDetails().get("errorCode"));
        assertEquals("testuser", errorResponse.getDetails().get("rejectedValue"));
    }

    @Test
    void testInvalidCredentialsException() {
        // Given
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        WebRequest webRequest = new ServletRequestAttributes(request);
        
        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleInvalidCredentialsException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ErrorResponseDTO errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Invalid credentials", errorResponse.getMessage());
        assertEquals("Unauthorized", errorResponse.getError());
        assertEquals(401, errorResponse.getStatus());
        assertNotNull(errorResponse.getDetails());
        assertEquals("INVALID_CREDENTIALS", errorResponse.getDetails().get("errorCode"));
    }

    @Test
    void testUserAlreadyExistsException() {
        // Given
        UserAlreadyExistsException exception = new UserAlreadyExistsException("User already exists", "existinguser");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/register");
        WebRequest webRequest = new ServletRequestAttributes(request);
        
        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleUserAlreadyExistsException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        
        ErrorResponseDTO errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("User already exists", errorResponse.getMessage());
        assertEquals("Conflict", errorResponse.getError());
        assertEquals(409, errorResponse.getStatus());
        assertNotNull(errorResponse.getDetails());
        assertEquals("USER_ALREADY_EXISTS", errorResponse.getDetails().get("errorCode"));
        assertEquals("existinguser", errorResponse.getDetails().get("rejectedValue"));
    }

    @Test
    void testGenericBusinessLogicException() {
        // Given
        BusinessLogicException exception = new BusinessLogicException("Generic business error", "GENERIC_ERROR");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        WebRequest webRequest = new ServletRequestAttributes(request);
        
        // When
        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleBusinessLogicException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponseDTO errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Generic business error", errorResponse.getMessage());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals(400, errorResponse.getStatus());
        assertNotNull(errorResponse.getDetails());
        assertEquals("GENERIC_ERROR", errorResponse.getDetails().get("errorCode"));
    }
}