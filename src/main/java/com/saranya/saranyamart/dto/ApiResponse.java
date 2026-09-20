package com.saranya.saranyamart.dto;

/**
 * Fixed response envelope for all JSON endpoints (spec Section 13 rule 2):
 * {@code { "success": true, "data": { ... }, "error": null }}.
 *
 * @param <T> the payload type
 */
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final Error error;

    private ApiResponse(boolean success, T data, Error error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    /**
     * Creates a successful envelope carrying a payload.
     *
     * @param data the payload
     * @param <T>  the payload type
     * @return the envelope
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * Creates a failed envelope carrying an error code and message.
     *
     * @param code    the machine-readable error code
     * @param message the human-readable message
     * @param <T>     the payload type
     * @return the envelope
     */
    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, null, new Error(code, message));
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public Error getError() {
        return error;
    }

    /**
     * Structured error detail carried by the envelope.
     */
    public static final class Error {

        private final String code;
        private final String message;

        Error(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
