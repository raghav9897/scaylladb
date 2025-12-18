package com.example.scyalladb.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HeartbeatResponse {

    private boolean ttlRefreshed;
    private String sessionId;
}

