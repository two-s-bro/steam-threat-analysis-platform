package com.steamthreat.dto;

import lombok.*;

/**
 * 统一 API 响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
