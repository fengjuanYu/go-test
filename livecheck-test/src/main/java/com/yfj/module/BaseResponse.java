package com.yfj.module;


import com.yfj.enums.ApiResult;
import lombok.Data;

@Data
public class BaseResponse<T> {

    private T data;

    private ApiResult result;

    private String message;

    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.data = data;
        response.result = ApiResult.Successful;
        response.message = null;
        return response;
    }

    public static <T> BaseResponse<T> fail(String errorMessage) {
        BaseResponse<T> response = new BaseResponse<>();
        response.data = null;
        response.result = ApiResult.Failed;
        response.message = errorMessage;
        return response;
    }
}
