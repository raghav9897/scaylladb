package com.example.scyalladb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class FetchDeviceDTO {
    private String deviceId;
    private Instant lastSeen;   // Use Instant for ISO 8601 timestamp
    private String deviceType;
}
