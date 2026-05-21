package com.studentbag.backend.chatbot.exception;
import com.studentbag.backend.chatbot.dto.response.AiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "com.studentbag.backend.ai")
public class AiExceptionHandler {

    @ExceptionHandler(AiConversationNotFoundException.class)
    public ResponseEntity<AiErrorResponse> handleConversationNotFound(AiConversationNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, AiErrorCode.AI_CONVERSATION_NOT_FOUND.name(), ex.getMessage());
    }

    @ExceptionHandler(AiMessageNotFoundException.class)
    public ResponseEntity<AiErrorResponse> handleMessageNotFound(AiMessageNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, AiErrorCode.AI_MESSAGE_NOT_FOUND.name(), ex.getMessage());
    }

    @ExceptionHandler(AiStudentNotFoundException.class)
    public ResponseEntity<AiErrorResponse> handleStudentNotFound(AiStudentNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, AiErrorCode.AI_STUDENT_NOT_FOUND.name(), ex.getMessage());
    }

    @ExceptionHandler(AiAccessDeniedException.class)
    public ResponseEntity<AiErrorResponse> handleAccessDenied(AiAccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, AiErrorCode.AI_ACCESS_DENIED.name(), ex.getMessage());
    }

    @ExceptionHandler(AiUsageLimitExceededException.class)
    public ResponseEntity<AiErrorResponse> handleUsageLimit(AiUsageLimitExceededException ex) {
        return build(HttpStatus.TOO_MANY_REQUESTS, AiErrorCode.AI_USAGE_LIMIT_EXCEEDED.name(), ex.getMessage());
    }

    @ExceptionHandler(AiInvalidRequestException.class)
    public ResponseEntity<AiErrorResponse> handleInvalidRequest(AiInvalidRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, AiErrorCode.AI_INVALID_REQUEST.name(), ex.getMessage());
    }

    @ExceptionHandler(AiContextBuildException.class)
    public ResponseEntity<AiErrorResponse> handleContextBuild(AiContextBuildException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, AiErrorCode.AI_CONTEXT_BUILD_FAILED.name(), ex.getMessage());
    }

    @ExceptionHandler(AiProviderException.class)
    public ResponseEntity<AiErrorResponse> handleProvider(AiProviderException ex) {
        return build(HttpStatus.BAD_GATEWAY, AiErrorCode.AI_PROVIDER_FAILED.name(), ex.getMessage());
    }

    @ExceptionHandler(AiException.class)
    public ResponseEntity<AiErrorResponse> handleAiException(AiException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "AI_ERROR", ex.getMessage());
    }

    private ResponseEntity<AiErrorResponse> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(
                AiErrorResponse.builder()
                        .code(code)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}