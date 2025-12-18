package com.example.scyalladb.service;


import com.example.scyalladb.dto.DeviceInfo;
import com.example.scyalladb.dto.request.DestroyPlaybackRequest;
import com.example.scyalladb.dto.request.FetchDeviceDTO;
import com.example.scyalladb.dto.request.HeartbeatRequest;
import com.example.scyalladb.dto.request.RegisterPlaybackRequest;
import com.example.scyalladb.dto.response.DestroyPlaybackResponse;
import com.example.scyalladb.dto.response.FetchDevicesResponse;
import com.example.scyalladb.dto.response.HeartbeatResponse;
import com.example.scyalladb.dto.response.RegisterPlaybackResponse;
import com.example.scyalladb.entity.ActivePlayback;
import com.example.scyalladb.repository.ActivePlaybackRepository;
import com.example.scyalladb.repository.PlaybackSessionLookupDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaybackService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PlaybackService.class);
    private final CassandraTemplate cassandraTemplate;
    private final ActivePlaybackRepository repository;
    private final PlaybackSessionLookupDao sessionLookupDao;

    private static final int TTL_SECONDS = 100;
    private static final int CONCURRENCY = 2;

    // Register Playback
    public RegisterPlaybackResponse register(RegisterPlaybackRequest req) {

        // 1️⃣ Fetch active devices
        List<FetchDeviceDTO> activeDevices =
                fetchActiveDevices(req.getSubscriberId());

        // 2️⃣ Concurrency check (race condition acceptable)
        if (activeDevices.size() >= CONCURRENCY) {
            throw new RuntimeException("Concurrent playback limit exceeded");
        }

        // 3️⃣ Generate session
        String sessionId = UUID.randomUUID().toString();

        // 4️⃣ Insert playback row with TTL
        insertActivePlayback(req, sessionId);

        // 5️⃣ Build response
        return RegisterPlaybackResponse.builder()
                .deviceId(req.getDeviceId())
                .sessionId(sessionId)
                .status("ACTIVE")
                .ttl(TTL_SECONDS)
                .build();
    }


    // Heartbeat
    public HeartbeatResponse heartbeat(HeartbeatRequest req) {

        // 1️⃣ Find playback by sessionId
        ActivePlayback existing = sessionLookupDao.findBySessionIdActivePlayback(req.getSessionId())
                .orElseThrow(() ->
                        new RuntimeException("Session expired or invalid")
                );

        // 2️⃣ Refresh lastSeen
        existing.setLastSeen(Instant.now());

        // 3️⃣ Re-insert with TTL = 10 seconds
        heartbeatQuery(existing);

        // 4️⃣ Build response
        return HeartbeatResponse.builder()
                .ttlRefreshed(true)
                .sessionId(req.getSessionId())
                .build();
    }


    // Fetch Active Devices
    public FetchDevicesResponse fetchDevices(
            String subscriberId,
            String sessionId
    ) {

        // 1️⃣ Validate requesting session
        ActivePlayback requester = sessionLookupDao.findBySessionIdActivePlayback(sessionId)
                .orElseThrow(() ->
                        new RuntimeException("Session expired or invalid")
                );

        // Extra safety: subscriber mismatch
        if (!requester.getKey().getSubscriberId().equals(subscriberId)) {
            throw new RuntimeException("Session does not belong to subscriber");
        }

        // 2️⃣ Fetch all active playbacks
        List<ActivePlayback> playbacks =
                repository.findByKeySubscriberId(subscriberId);

        // 3️⃣ Build device list
        List<DeviceInfo> devices = playbacks.stream()
                .map(p -> DeviceInfo.builder()
                        .sessionId(p.getSessionId())
                        .deviceId(p.getKey().getDeviceId())
                        .status(p.getStatus())
                        .lastSeen(p.getLastSeen())
                        .deviceType(p.getDeviceType())
                        .os(p.getOs())
                        .build())
                .toList();

        // 4️⃣ Determine requester status
        boolean requesterActive = playbacks.stream()
                .anyMatch(p -> p.getSessionId().equals(sessionId));

        return FetchDevicesResponse.builder()
                .sessionId(sessionId)
                .status(requesterActive ? "ACTIVE" : "FORCE_STOP")
                .requestedBySessionId(sessionId)
                .requestedByDeviceId(requester.getKey().getDeviceId())
                .ongoingSession(devices)
                .build();
    }


    // Destroy Playback
    public DestroyPlaybackResponse destroy(DestroyPlaybackRequest request) {

        ActivePlayback existing = repository
                .findByKeySubscriberIdAndKeyDeviceId(
                        request.getSubscriberId(),
                        request.getDeviceId()
                )
                .orElse(null);

        if (existing == null) {
             throw new RuntimeException("Playback not found");
        }

        // Mark FORCE_STOP
        existing.setStatus("FORCE_STOP");
        existing.setLastSeen(Instant.now());

        // Reset TTL = 10 * base TTL
        forceStopWithExtendedTtl(existing);

        return  DestroyPlaybackResponse.builder()
                .deviceId(request.getDeviceId())
                .build();


    }


    private void insertActivePlayback(
            RegisterPlaybackRequest req,
            String sessionId
    ) {
        String cql = """
        INSERT INTO active_playback (
            subscriber_id,
            device_id,
            session_id,
            playback_token,
            device_type,
            app_version,
            os,
            content_type,
            content_id,
            x_stream_id,
            status,
            last_seen
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, toTimestamp(now()))
        USING TTL ?
    """;

        cassandraTemplate.getCqlOperations().execute(
                cql,
                req.getSubscriberId(),
                req.getDeviceId(),
                sessionId,
                req.getPlaybackToken(),
                req.getDeviceType(),
                req.getAppVersion(),
                req.getOs(),
                req.getContentType(),
                req.getContentId(),
                req.getXStreamId(),
                "ACTIVE",
                TTL_SECONDS
        );
    }


    public boolean heartbeatQuery(ActivePlayback existing) {

        String cql = """
        INSERT INTO active_playback (
            subscriber_id,
            device_id,
            session_id,
            playback_token,
            device_type,
            app_version,
            os,
            content_type,
            content_id,
            x_stream_id,
            status,
            last_seen
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        USING TTL ?
    """;

        return cassandraTemplate.getCqlOperations().execute(
                cql,
                existing.getKey().getSubscriberId(),
                existing.getKey().getDeviceId(),
                existing.getSessionId(),
                existing.getPlaybackToken(),
                existing.getDeviceType(),
                existing.getAppVersion(),
                existing.getOs(),
                existing.getContentType(),
                existing.getContentId(),
                existing.getXStreamId(),
                existing.getStatus(),
                Instant.now(),
                TTL_SECONDS
        );
    }


    private boolean forceStopWithExtendedTtl(ActivePlayback existing) {

        int forceStopTtl = TTL_SECONDS * 10;

        String cql = """
        INSERT INTO active_playback (
            subscriber_id,
            device_id,
            status,
            last_seen,
            device_type,
            playback_token,
            app_version,
            os,
            ip
        ) VALUES (?, ?, ?, toTimestamp(now()), ?, ?, ?, ?, ?)
        USING TTL ?
    """;

        return cassandraTemplate.getCqlOperations().execute(
                cql,
                existing.getKey().getSubscriberId(),
                existing.getKey().getDeviceId(),
                "FORCE_STOP",
                existing.getDeviceType(),
                existing.getPlaybackToken(),
                existing.getAppVersion(),
                existing.getOs(),
                existing.getIp(),
                forceStopTtl
        );
    }


    public List<FetchDeviceDTO> fetchActiveDevices(String subscriberId) {
        log.info("Fetching active devices for subscriber={}", subscriberId);
        List<ActivePlayback> devices = repository.findByKeySubscriberId(subscriberId);
        log.info("Found {} active devices for subscriber={}", devices.size(), subscriberId);
        devices.forEach(x->log.info("{}, ={},={}",x.getKey().getDeviceId(),x.getLastSeen(),x.getDeviceType()));
        List<FetchDeviceDTO> active = devices.stream() .map(x->new FetchDeviceDTO(x.getKey().getDeviceId(),x.getLastSeen(),x.getDeviceType())) .collect(Collectors.toUnmodifiableList());
        active.forEach(x->log.info("{}, ={},={}",x.getDeviceId(),x.getLastSeen(),x.getDeviceType()));
        return active; }
}

