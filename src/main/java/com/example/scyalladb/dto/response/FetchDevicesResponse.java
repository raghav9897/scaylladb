package com.example.scyalladb.dto.response;

import com.example.scyalladb.dto.DeviceInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class FetchDevicesResponse {
    private String sessionId;
    private String status; // ACTIVE / FORCE_STOP
    private String requestedBySessionId;
    private String requestedByDeviceId;
    private List<DeviceInfo> ongoingSession;
}
