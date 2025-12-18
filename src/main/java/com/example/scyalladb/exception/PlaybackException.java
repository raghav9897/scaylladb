package com.example.scyalladb.exception;

import com.example.scyalladb.enums.ApiResponseCode;

public class PlaybackException extends RuntimeException {
    private final int code;
    private final String message;
    private String localizedMessage;

    public PlaybackException(ApiResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage;
    }

    public PlaybackException setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = localizedMessage;
        return this;
    }
}
