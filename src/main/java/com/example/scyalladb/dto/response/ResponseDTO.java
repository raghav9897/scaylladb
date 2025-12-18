package com.example.scyalladb.dto.response;

public interface ResponseDTO<T> {

    int getCode();

    void setCode(int code);

    String getMessage();

    void setMessage(String message);

    T getData();

    void setData(T data);
}
