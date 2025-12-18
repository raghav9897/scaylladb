package com.example.scyalladb.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HeartbeatRequest {
    @NotBlank
    private String subscriberId;


    @NotBlank
    private String deviceId;

    @NotBlank
    private String sessionId;
}
