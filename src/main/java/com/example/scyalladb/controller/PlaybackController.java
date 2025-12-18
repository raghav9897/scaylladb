package com.example.scyalladb.controller;

import com.example.scyalladb.dto.request.DestroyPlaybackRequest;
import com.example.scyalladb.dto.request.HeartbeatRequest;
import com.example.scyalladb.dto.request.RegisterPlaybackRequest;
import com.example.scyalladb.dto.response.DestroyPlaybackResponse;
import com.example.scyalladb.dto.response.ResponseDTO;
import com.example.scyalladb.service.PlaybackService;
import com.example.scyalladb.utils.response.ResponseUtil;
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
    private final ResponseUtil responseUtil;


    @PostMapping("/register")
    public ResponseDTO register(@Valid @RequestBody RegisterPlaybackRequest req,
                                @RequestHeader(value = "locale", defaultValue = "en") String locale,
                                @RequestHeader(value="true-client-ip",required = false) String trueClientIp,
                                @RequestHeader(value = "x-authenticated-userid", required = false) String sid,
                                @RequestHeader(value="tpr-id",required = false) String tprId,
                                @RequestHeader(value ="platform", defaultValue = "lit_android") String platform) {



        return responseUtil.toSuccess(service.register(req),null);

    }


    @PostMapping("/heartbeat")
    public ResponseDTO heartbeat(@Valid @RequestBody HeartbeatRequest req,
                                 @RequestHeader(value = "locale", defaultValue = "en") String locale,
                                 @RequestHeader(value="true-client-ip",required = false) String trueClientIp,
                                 @RequestHeader(value = "x-authenticated-userid", required = false) String sid,
                                 @RequestHeader(value="tpr-id",required = false) String tprId,
                                 @RequestHeader(value ="platform", defaultValue = "lit_android") String platform) {

        return responseUtil.toSuccess(service.heartbeat(req),locale);
    }


    @GetMapping("/{subscriberId}/{sessionId}/devices")
    public ResponseDTO activeDevices(@PathVariable String subscriberId,@PathVariable String sessionId,
                                     @RequestHeader(value = "locale", defaultValue = "en") String locale,
                                     @RequestHeader(value="true-client-ip",required = false) String trueClientIp,
                                     @RequestHeader(value = "x-authenticated-userid", required = false) String sid,
                                     @RequestHeader(value="tpr-id",required = false) String tprId,
                                     @RequestHeader(value ="platform", defaultValue = "lit_android") String platform) {
        return responseUtil.toSuccess(service.fetchDevices(subscriberId,sessionId),locale);
    }


   /* @PostMapping("/destroy")
    public ResponseDTO destroy(@Valid @RequestBody DestroyPlaybackRequest req,@RequestHeader(value = "locale", defaultValue = "en") String locale,
                               @RequestHeader(value="true-client-ip",required = false) String trueClientIp,
                               @RequestHeader(value = "x-authenticated-userid", required = true) String sid,
                               @RequestHeader(value="tpr-id",required = false) String tprId,
                               @RequestHeader(value ="platform", defaultValue = "lit_android") String platform) {
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


    }*/

    @PostMapping("/destroy")
    public ResponseDTO destroy(@Valid @RequestBody DestroyPlaybackRequest request,
                               @RequestHeader(value = "locale", defaultValue = "en") String locale,
                               @RequestHeader(value="true-client-ip",required = false) String trueClientIp,
                               @RequestHeader(value = "x-authenticated-userid", required = false) String sid,
                               @RequestHeader(value="tpr-id",required = false) String tprId,
                               @RequestHeader(value ="platform", defaultValue = "lit_android") String platform) {
        return responseUtil.toSuccess(service.destroy(request),locale);
    }
}

