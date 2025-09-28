package com.couple.numberbaseball.dto;

/**
 * 기본 API 응답 형태
 */
public class ApiResponseDto<T> {
    private boolean success;
    private String message;
    private T data;

    public ApiResponseDto() {}

    public ApiResponseDto(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 성공 응답 생성
    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(true, "성공", data);
    }

    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, data);
    }

    // 실패 응답 생성
    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(false, message, null);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}