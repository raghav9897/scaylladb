package com.example.scyalladb.controller;

import com.example.scyalladb.dto.DestroyPlaybackRequest;
import com.example.scyalladb.dto.HeartbeatRequest;
import com.example.scyalladb.dto.RegisterPlaybackRequest;
import com.example.scyalladb.service.PlaybackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/playback")
@RequiredArgsConstructor
public class PlaybackController {


    private final PlaybackService service;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterPlaybackRequest req) {
        boolean s = service.register(req);

        if (!s){
             return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "code", 1,
                            "message", "Playback registration failed",
                            "data", Map.of()
                    ));

        }

        return ResponseEntity.ok(Map.of(
                "code", 0,
                "message", "Playback registered successfully",
                "data", Map.of(
                        "deviceId", req.getDeviceId(),
                        "ttl", 10
                )
        ));
    }


    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@Valid @RequestBody HeartbeatRequest req) {
        boolean updated = service.heartbeat(req.getSubscriberId(), req.getDeviceId());
        return ResponseEntity.ok(Map.of(
                "code", 0,
                "message", "Heartbeat updated",
                "data", Map.of("ttlRefreshed", updated)
        ));
    }


    @GetMapping("/{subscriberId}/devices")
    public ResponseEntity<?> activeDevices(@PathVariable String subscriberId) {
        return ResponseEntity.ok(Map.of(
                "code", 0,
                "message", "Success",
                "data", Map.of("devices", service.fetchActiveDevices(subscriberId))
        ));
    }


    @PostMapping("/destroy")
    public ResponseEntity<?> destroy(@Valid @RequestBody DestroyPlaybackRequest req) {
       boolean isDestroyed = service.destroy(req.getSubscriberId(), req.getDeviceId());
       if(!isDestroyed){
          return ResponseEntity
                   .status(HttpStatus.BAD_REQUEST)
                   .body(Map.of(
                           "code", 1,
                           "message", "Playback destroyed failed",
                           "data", Map.of()
                   ));
       }
        return ResponseEntity.ok(Map.of(
                "code", 0,
                "message", "Playback destroyed",
                "data", Map.of("deviceId", req.getDeviceId())
        ));
    }
}

