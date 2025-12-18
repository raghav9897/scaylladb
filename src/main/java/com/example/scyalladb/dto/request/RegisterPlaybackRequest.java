package com.example.scyalladb.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterPlaybackRequest {

    @NotBlank
    private String subscriberId;

    @NotBlank
    private String deviceId;

    @NotBlank
    private String playbackToken;

    private String deviceType;
    private String appVersion;
    private String os;
    private String ip;
    private String contentType;
    private String contentId;
    private String xStreamId;
}

