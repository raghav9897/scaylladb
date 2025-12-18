package com.example.scyalladb.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DestroyPlaybackRequest {
    @NotBlank
    private String subscriberId;


    @NotBlank
    private String deviceId;


    private String sessionId;
}
