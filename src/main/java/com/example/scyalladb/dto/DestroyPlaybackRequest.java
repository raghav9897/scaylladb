package com.example.scyalladb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DestroyPlaybackRequest {
    @NotBlank
    private String subscriberId;


    @NotBlank
    private String deviceId;


    private String reason;
}
