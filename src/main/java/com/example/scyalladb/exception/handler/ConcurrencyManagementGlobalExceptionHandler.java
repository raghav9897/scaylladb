package com.example.scyalladb.exception.handler;

import com.example.scyalladb.dto.response.ApiResponseDTO;
import com.example.scyalladb.dto.response.ResponseDTO;
import com.example.scyalladb.exception.PlaybackException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;

public class ConcurrencyManagementGlobalExceptionHandler {

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(value = {PlaybackException.class})
    public ResponseDTO handlePortalRedirectionApiException(PlaybackException e) {
        if(e.getLocalizedMessage()!=null) {
            return new ApiResponseDTO(e.getCode(), e.getMessage(), new HashMap<>(),e.getLocalizedMessage());
        }

        return new ApiResponseDTO(e.getCode(),e.getMessage(),new HashMap<>());
    }
}
