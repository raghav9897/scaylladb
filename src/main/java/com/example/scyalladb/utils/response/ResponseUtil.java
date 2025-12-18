package com.example.scyalladb.utils.response;

import com.example.scyalladb.dto.response.ApiResponseDTO;
import com.example.scyalladb.dto.response.ResponseDTO;
import com.example.scyalladb.enums.ApiResponseCode;
import com.example.scyalladb.enums.abstraction.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

@Component
public class ResponseUtil {

    @Autowired
    private MessageSource messageSource;

    private Locale getLocale(String locale) {
        return locale != null ? new Locale(locale) : Locale.UK;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseDTO toSuccess(Object data, String locale) {
        String message = messageSource.getMessage(String.valueOf(ApiResponseCode.SUCCESS.getCode()), null, Locale.UK);
        String localizedMessage = messageSource.getMessage(String.valueOf(ApiResponseCode.SUCCESS.getCode()), null, getLocale(locale));
        return new ApiResponseDTO(ApiResponseCode.SUCCESS.getCode(), message,data, localizedMessage);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseDTO toSuccess(Object data, ResponseCode responseCode, String locale) {
        String message = messageSource.getMessage(String.valueOf(responseCode.getCode()), null, Locale.UK);
        String localizedMessage = messageSource.getMessage(String.valueOf(responseCode.getCode()), null, getLocale(locale));
        return new ApiResponseDTO(responseCode.getCode(), message,data, localizedMessage);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseDTO withException(int code) {
        String message = messageSource.getMessage(String.valueOf(code), null, getLocale(null));
        return new ApiResponseDTO(code, message, new HashMap<>());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseDTO withException(ResponseCode responseCode) {
        String message = messageSource.getMessage(String.valueOf(responseCode.getCode()), null, getLocale(null));
        return new ApiResponseDTO(responseCode.getCode(), message, new HashMap<>());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseDTO exception(int code, String message) {
        if (Objects.isNull(message) || message.trim().isEmpty()) {
            message = messageSource.getMessage(String.valueOf(code), null, getLocale(null));
        } else {
            message = messageSource.getMessage(String.valueOf(message), null, getLocale(null));
        }
        return new ApiResponseDTO(code, message, new HashMap<>());
    }

    public ResponseDTO exception(int code, String message, String localizedMessage) {
        message = messageSource.getMessage(String.valueOf(code), null, getLocale(null));
        return new ApiResponseDTO(code,message,new HashMap<>(),localizedMessage);
    }
}
