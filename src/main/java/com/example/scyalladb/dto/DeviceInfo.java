package com.example.scyalladb.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DeviceInfo {

    private String sessionId;
    private String deviceId;
    private String status;
    private Instant lastSeen;
    private String deviceType;
    private String os;
}

