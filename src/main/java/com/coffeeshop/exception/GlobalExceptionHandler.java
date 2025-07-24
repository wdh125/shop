package com.coffeeshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Lớp này hoạt động như một bộ xử lý ngoại lệ tập trung cho toàn bộ ứng dụng.
 * Annotation @ControllerAdvice cho phép nó "lắng nghe" các Exception được ném ra từ bất kỳ Controller nào.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt và xử lý các lỗi liên quan đến trạng thái không hợp lệ của đối tượng.
     * Ví dụ: Cố gắng thanh toán cho một đơn hàng đã được thanh toán.
     *
     * @param ex      Ngoại lệ IllegalStateException được ném ra.
     * @param request WebRequest chứa thông tin về request gây ra lỗi.
     * @return một ResponseEntity với mã lỗi 409 (Conflict) và thông điệp lỗi rõ ràng.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).substring(4)); // Lấy URI của request

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Bắt và xử lý các lỗi RuntimeException chung.
     * Phù hợp cho các trường hợp như không tìm thấy tài nguyên (user, order...).
     *
     * @param ex      Ngoại lệ RuntimeException được ném ra.
     * @param request WebRequest chứa thông tin về request gây ra lỗi.
     * @return một ResponseEntity với mã lỗi 404 (Not Found) hoặc 400 (Bad Request).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        HttpStatus status;
        String error;

        // Nếu thông điệp lỗi chứa "not found", chúng ta coi đó là lỗi 404.
        if (ex.getMessage().toLowerCase().contains("not found")) {
            status = HttpStatus.NOT_FOUND;
            error = "Not Found";
        } else {
            // Các lỗi runtime khác sẽ được coi là 400 Bad Request.
            status = HttpStatus.BAD_REQUEST;
            error = "Bad Request";
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, status);
    }
}
