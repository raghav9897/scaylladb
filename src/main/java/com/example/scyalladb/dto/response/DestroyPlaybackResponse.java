package com.example.scyalladb.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DestroyPlaybackResponse {

    private String deviceId;
}
