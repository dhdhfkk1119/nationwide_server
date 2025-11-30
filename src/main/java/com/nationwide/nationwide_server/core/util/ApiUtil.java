package com.nationwide.nationwide_server.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

// 공통 API 응답 형식을 위한 유틸리티 클래스
public class ApiUtil<T> {

    // 성공 응답을 생성하는 정적 메서드
    public static <T> ApiResult<T> success(T response) {
        return new ApiResult<>(true, response, null);
    }

    // 실패 응답을 생성하는 정적 메서드 (메시지와 상태 코드만 포함)
    public static <T> ApiResult<T> fail(String errorMessage, HttpStatus status) {
        return new ApiResult<>(false, null, new ApiError(errorMessage, status.value()));
    }

    // 실패 응답을 생성하는 정적 메서드 (메시지, 상태 코드, 에러 코드 포함)
    public static <T> ApiResult<T> fail(String errorMessage, HttpStatus status, String errorCode) {
        return new ApiResult<>(false, null, new ApiError(errorMessage, status.value(), errorCode));
    }

    // 실패 응답을 생성하는 정적 메서드 (유효성 검사 오류 포함)
    public static <T> ApiResult<T> fail(String errorMessage, HttpStatus status, String errorCode, Map<String, String> validationErrors) {
        return new ApiResult<>(false, null, new ApiError(errorMessage, status.value(), errorCode, validationErrors));
    }

    @Data
    public static class ApiResult<T> {
        private final boolean success;
        private final T response;
        private final ApiError error;

        private ApiResult(boolean success, T response, ApiError error) {
            this.success = success;
            this.response = response;
            this.error = error;
        }
    }

    @Data
    public static class ApiError {
        private final String message;
        private final int status;
        @JsonInclude(JsonInclude.Include.NON_NULL) // code가 null이면 JSON에서 제외
        private final String code;
        @JsonInclude(JsonInclude.Include.NON_NULL) // validationErrors가 null이면 JSON에서 제외
        private final Map<String, String> validationErrors;

        // 기존 생성자
        private ApiError(String message, int status) {
            this(message, status, null, null);
        }

        // 에러 코드를 받는 새로운 생성자
        private ApiError(String message, int status, String code) {
            this(message, status, code, null);
        }

        // 유효성 검사 오류를 포함하는 최종 생성자
        private ApiError(String message, int status, String code, Map<String, String> validationErrors) {
            this.message = message;
            this.status = status;
            this.code = code;
            this.validationErrors = validationErrors;
        }
    }

}
