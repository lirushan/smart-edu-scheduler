package com.smartedu.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果
 *
 * @param <T> 数据泛型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {

    @Schema(description = "状态码", example = "200")
    private int code;

    @Schema(description = "响应消息", example = "success")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "链路追踪ID")
    private String traceId;

    @Schema(description = "时间戳")
    private long timestamp;

    // ===== 成功响应 =====

    public static <T> Result<T> ok() {
        return new Result<>(200, "success", null, null, System.currentTimeMillis());
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data, null, System.currentTimeMillis());
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, message, data, null, System.currentTimeMillis());
    }

    // ===== 失败响应 =====

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, null, System.currentTimeMillis());
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(400, message, null, null, System.currentTimeMillis());
    }

    // ===== 业务错误码 =====

    public static <T> Result<T> error(BizError error) {
        return new Result<>(error.getCode(), error.getMessage(), null, null, System.currentTimeMillis());
    }
}
