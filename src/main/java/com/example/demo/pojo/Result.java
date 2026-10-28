package com.example.demo.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Result<T>{
    private T data;
    private String message;
    private int code;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setData(data);
        result.setCode(1);
        result.setMessage("成功");
        return result;
    }
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMessage("成功");
        return result;
    }
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage(message);
        return result;
    }
}
