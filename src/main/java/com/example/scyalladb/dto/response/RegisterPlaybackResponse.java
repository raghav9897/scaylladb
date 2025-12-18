package com.example.scyalladb.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterPlaybackResponse {

    private String deviceId;
    private String sessionId;
    private String status;
    private int ttl;
}
