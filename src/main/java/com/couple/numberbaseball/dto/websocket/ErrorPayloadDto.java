package com.couple.numberbaseball.dto.websocket;

/**
 * 오류 메시지 페이로드
 */
public class ErrorPayloadDto {
    private String errorCode;
    private String message;
    private String details;

    public ErrorPayloadDto() {}

    public ErrorPayloadDto(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public ErrorPayloadDto(String errorCode, String message, String details) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
    }

    // Getters and Setters
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}