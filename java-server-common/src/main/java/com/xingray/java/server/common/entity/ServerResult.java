package com.xingray.java.server.common.entity;

import java.util.function.Function;

public class ServerResult<T> {


    private final boolean success;
    private final T data;
    private final int code;
    private final String message;

    public static final int CODE_FAIL_DEFAULT = -1;
    public static final int CODE_SUCCESS_DEFAULT = 0;
    public static final String MESSAGE_FAIL_DEFAULT = "unknown error";
    public static final String MESSAGE_SUCCESS_DEFAULT = "success";
    public static final ServerResult<Object> OK = new ServerResult<>(true);
    public static final ServerResult<Object> FAIL = new ServerResult<>(false, null, CODE_FAIL_DEFAULT, MESSAGE_FAIL_DEFAULT);

    public static <V> ServerResult<V> result(boolean success) {
        return success ? success() : failure();
    }

    public static <V> ServerResult<V> success() {
        //noinspection unchecked
        return (ServerResult<V>) OK;
    }

    public static <V> ServerResult<V> success(V v) {
        return new ServerResult<>(true, v, CODE_SUCCESS_DEFAULT, MESSAGE_SUCCESS_DEFAULT);
    }

    public static <V, U> ServerResult<V> of(ServerResult<U> serverResult, Function<U, V> function) {
        boolean isSuccess = serverResult.isSuccess();
        if (isSuccess) {
            V data = function.apply(serverResult.getData());
            return new ServerResult<>(true, data, serverResult.getCode(), serverResult.getMessage());
        } else {
            return new ServerResult<>(false, null, serverResult.getCode(), serverResult.getMessage());
        }
    }

    public static <V, U> ServerResult<V> of(ServerResult<?> serverResult) {
        return new ServerResult<>(serverResult.isSuccess(), null, serverResult.getCode(), serverResult.getMessage());
    }

    public static <V> ServerResult<V> failure() {
        //noinspection unchecked
        return (ServerResult<V>) FAIL;
    }

    public static <V> ServerResult<V> failure(int code, String message) {
        return new ServerResult<>(false, null, code, message);
    }

    public static <V> ServerResult<V> failure(int code) {
        return failure(code, MESSAGE_FAIL_DEFAULT);
    }

    public static <V> ServerResult<V> failure(String message) {
        return failure(CODE_FAIL_DEFAULT, message);
    }

    public static <V> ServerResult<V> failure(Exception e) {
        return failure(CODE_FAIL_DEFAULT, e.getMessage());
    }
    public ServerResult() {
        this(true, null, CODE_SUCCESS_DEFAULT, MESSAGE_SUCCESS_DEFAULT);
    }

    public ServerResult(boolean success) {
        this(success, null, success ? 0 : CODE_FAIL_DEFAULT, success ? MESSAGE_SUCCESS_DEFAULT : MESSAGE_FAIL_DEFAULT);
    }

    public ServerResult(boolean success, T data, int code, String message) {
        this.data = data;
        this.success = success;
        this.message = message;
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ServerResult{" +
                "success=" + success +
                ", data=" + data +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
