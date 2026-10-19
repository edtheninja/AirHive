package com.airhive.backend.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleNotFound(
                        ResourceNotFoundException exception) {

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(Map.of(
                                                "status", HttpStatus.NOT_FOUND.value(),
                                                "message", exception.getMessage()));
        }

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(RedisConnectionFailureException.class)
        public ResponseEntity<Map<String, Object>> handleRedisConnectionFailure(
                        RedisConnectionFailureException exception) {

                logger.error("Redis is unavailable; a cache operation failed.", exception);

                return ResponseEntity
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(Map.of(
                                                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                                                "message", "A required service is temporarily unavailable"));
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<Map<String, Object>> handleDuplicate(
                        DuplicateResourceException exception) {

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(Map.of(
                                                "status", HttpStatus.CONFLICT.value(),
                                                "message", exception.getMessage()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidation(
                        MethodArgumentNotValidException exception) {

                Map<String, String> errors = new LinkedHashMap<>();

                exception.getBindingResult()
                                .getFieldErrors()
                                .forEach(error -> errors.put(
                                                error.getField(),
                                                error.getDefaultMessage()));

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(
                                                "status", HttpStatus.BAD_REQUEST.value(),
                                                "message", "Validation failed",
                                                "errors", errors));
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<Map<String, Object>> handleBadCredentials(
                        BadCredentialsException exception) {

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of(
                                                "status", HttpStatus.UNAUTHORIZED.value(),
                                                "message", "Invalid username or password"));
        }

        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<Map<String, Object>> handleDisabledUser(
                        DisabledException exception) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of(
                                                "status", HttpStatus.UNAUTHORIZED.value(),
                                                "message", "Invalid username or password"));
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<Map<String, Object>> handleRuntimeException(
                        RuntimeException exception) {

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(
                                                "status", HttpStatus.BAD_REQUEST.value(),
                                                "message", exception.getMessage()));
        }

        @ExceptionHandler(ResourceInUseException.class)
        public ResponseEntity<Map<String, Object>> handleResourceInUse(
                        ResourceInUseException exception) {

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(Map.of(
                                                "status", HttpStatus.CONFLICT.value(),
                                                "message", exception.getMessage()));
        }
}