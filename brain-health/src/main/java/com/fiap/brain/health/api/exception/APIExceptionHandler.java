package com.fiap.brain.health.api.exception;

import com.fiap.brain.health.api.dto.error.ProblemDetail;
import com.fiap.brain.health.api.dto.error.ValidationError;
import com.fiap.brain.health.domain.exception.*;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import com.fiap.brain.health.infrastructure.exception.ExternalServiceException;
import com.fiap.brain.health.infrastructure.exception.InfrastructureException;
import com.fiap.brain.health.infrastructure.exception.KafkaMessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Global Exception Handler - RFC 7807 Compliant
 * Implements Problem Details for HTTP APIs standard.
 * Handles all exceptions across the microservice with proper logging and tracing.
 * Exception Hierarchy:
 * - Domain Exceptions (4xx errors) - Business logic violations
 * - Infrastructure Exceptions (5xx errors) - Technical failures
 * - Validation Exceptions (400 errors) - Input validation failures
 */
@Slf4j
@RestControllerAdvice
public class APIExceptionHandler {

    private static final String PROBLEM_BASE_URL = "https://brain-health.fiap.com/problems/";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("[{}] Resource not found: {} - {}", traceId, ex.getResourceType(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "resource-not-found")
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "resourceType", ex.getResourceType(),
                    "resourceId", ex.getResourceIdentifier()
                ))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleArticleNotFound(
            ArticleNotFoundException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("[{}] Article not found: {}", traceId, ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "article-not-found")
                .title("Medical Article Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "suggestion", "Try using different search terms or check the topic spelling"
                ))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("[{}] Business rule violation: {} - {}", traceId, ex.getErrorCode(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "business-rule-violation")
                .title("Business Rule Violation")
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "errorCode", ex.getErrorCode()
                ))
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            ValidationException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("[{}] Domain validation failed: {}", traceId, ex.getMessage());

        List<ValidationError> errors = ex.getValidationErrors().entrySet().stream()
                .map(entry -> new ValidationError(entry.getKey(), entry.getValue()))
                .toList();

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "validation-error")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("One or more validation errors occurred")
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "validationErrors", errors
                ))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("[{}] Request validation failed: {}", traceId, ex.getMessage());

        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationError(
                    error.getField(),
                    error.getDefaultMessage(),
                    error.getRejectedValue(),
                    null
                ))
                .toList();

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "invalid-request")
                .title("Invalid Request Parameters")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Request validation failed")
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "validationErrors", errors,
                    "errorCount", errors.size()
                ))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("[{}] Illegal argument: {}", traceId, ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "invalid-argument")
                .title("Invalid Argument")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ProblemDetail> handleExternalService(
            ExternalServiceException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.error("[{}] External service error [{}]: {}", traceId, ex.getServiceName(), ex.getMessage(), ex);

        HttpStatus status = ex.isRetryable()
            ? HttpStatus.SERVICE_UNAVAILABLE
            : HttpStatus.BAD_GATEWAY;

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "external-service-error")
                .title("External Service Error")
                .status(status.value())
                .detail(String.format("Error communicating with %s: %s", ex.getServiceName(), ex.getMessage()))
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "serviceName", ex.getServiceName(),
                    "retryable", ex.isRetryable(),
                    "retryAfter", ex.isRetryable() ? "60 seconds" : "Not retryable"
                ))
                .build();

        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(AIProcessingPort.AIProcessingException.class)
    public ResponseEntity<ProblemDetail> handleAIProcessing(
            AIProcessingPort.AIProcessingException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.error("[{}] AI processing error: {}", traceId, ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "ai-processing-error")
                .title("AI Processing Failed")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("Failed to process content with AI: " + ex.getMessage())
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "service", "OpenAI",
                    "suggestion", "The AI service may be temporarily unavailable. Please retry."
                ))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(ArticleSearchException.class)
    public ResponseEntity<ProblemDetail> handleArticleSearch(
            ArticleSearchException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.error("[{}] Article search error: {}", traceId, ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "article-search-error")
                .title("Article Search Failed")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "service", "CREMESP",
                    "retryable", true
                ))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(KafkaMessagingException.class)
    public ResponseEntity<ProblemDetail> handleKafkaMessaging(
            KafkaMessagingException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.error("[{}] Kafka messaging error [{}]: {}", traceId, ex.getTopic(), ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "messaging-error")
                .title("Messaging Service Error")
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .detail("Kafka messaging operation failed: " + ex.getMessage())
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "topic", ex.getTopic(),
                    "operation", ex.getOperation(),
                    "retryable", true
                ))
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ProblemDetail> handleInfrastructure(
            InfrastructureException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.error("[{}] Infrastructure error: {}", traceId, ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "infrastructure-error")
                .title("Infrastructure Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An infrastructure error occurred: " + ex.getMessage())
                .instance(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<ProblemDetail> handleQuotaExceeded(
            HttpClientErrorException.TooManyRequests ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.error("[{}] Rate limit exceeded", traceId, ex);

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "rate-limit-exceeded")
                .title("Rate Limit Exceeded")
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .detail("OpenAI API quota exceeded. Please add credits to your account.")
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "service", "OpenAI",
                    "action", "Add credits at: https://platform.openai.com/account/billing",
                    "retryAfter", "After adding credits"
                ))
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(problem);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("[{}] Unsupported media type: {}", traceId, ex.getContentType());

        String supportedTypes = ex.getSupportedMediaTypes().isEmpty()
            ? "application/json"
            : ex.getSupportedMediaTypes().toString();

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "unsupported-media-type")
                .title("Unsupported Media Type")
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .detail(String.format("Content-Type '%s' is not supported", ex.getContentType()))
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "receivedContentType", ex.getContentType() != null ? ex.getContentType().toString() : "none",
                    "supportedMediaTypes", supportedTypes,
                    "suggestion", "Use Content-Type: application/json"
                ))
                .build();

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("[{}] Malformed JSON request: {}", traceId, ex.getMessage());

        String detail = "Malformed JSON request";
        Throwable cause = ex.getCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null) {
                // Extract the core problem from Jackson error message
                if (causeMessage.contains("problem:")) {
                    int problemIndex = causeMessage.indexOf("problem:");
                    String problem = causeMessage.substring(problemIndex + 8).trim();
                    detail = problem.split("\n")[0]; // Get first line only
                } else if (causeMessage.contains("required") || causeMessage.contains("obrigatório")) {
                    detail = causeMessage;
                } else if (causeMessage.contains("Unexpected character")) {
                    detail = "Invalid JSON syntax";
                }
            }
        }

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "malformed-json")
                .title("Malformed JSON Request")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "suggestion", "Check your JSON syntax and ensure all required fields are present"
                ))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.error("[{}] Unexpected error: {}", traceId, ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.builder()
                .type(PROBLEM_BASE_URL + "internal-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred while processing your request")
                .instance(request.getRequestURI())
                .traceId(traceId)
                .extensions(Map.of(
                    "support", "Please contact support with trace ID: " + traceId
                ))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }

}
