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
import com.example.scyalladb.entity.PlaybackSessionLookup;
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
        PlaybackSessionLookup lookup = new PlaybackSessionLookup(
                sessionId,
                req.getSubscriberId(),
                req.getDeviceId(),
                "ACTIVE"
        );

        sessionLookupDao.saveWithTtl(lookup, TTL_SECONDS);

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

        // 1️⃣ Validate session
        PlaybackSessionLookup sessionLookup =
                sessionLookupDao.findBySessionId(req.getSessionId()).get();

        if (sessionLookup == null || !"ACTIVE".equals(sessionLookup.getStatus())) {
            throw new RuntimeException("Session expired or invalid");
        }

        // 2️⃣ Ensure session belongs to same subscriber + device
        if (!sessionLookup.getSubscriberId().equals(req.getSubscriberId())
                || !sessionLookup.getDeviceId().equals(req.getDeviceId())) {
            throw new RuntimeException("Session does not match device or subscriber");
        }

        // 3️⃣ Load active playback
        ActivePlayback playback =
                repository.findByKeySubscriberIdAndKeyDeviceId(
                        req.getSubscriberId(),
                        req.getDeviceId()
                ).get();

        if (playback == null) {
            throw new RuntimeException("Playback session expired");
        }

        // 4️⃣ Refresh lastSeen
        playback.setLastSeen(Instant.now());

        // 5️⃣ Refresh TTL on active_playback
        heartbeatQuery(playback);

        // 6️⃣ Refresh TTL on session lookup
        sessionLookupDao.saveWithTtl(sessionLookup, TTL_SECONDS);

        // 7️⃣ Response
        return HeartbeatResponse.builder()
                .ttlRefreshed(true)
                .sessionId(req.getSessionId())
                .build();
    }


    // Fetch Active Devices
    public FetchDevicesResponse fetchDevices(String subscriberId, String sessionId) {

        PlaybackSessionLookup sessionLookup = sessionLookupDao.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException(
                        "No active session found for sessionId " + sessionId));

        if (!"ACTIVE".equals(sessionLookup.getStatus())) {
            throw new RuntimeException("Session expired or invalid");
        }

        if (!sessionLookup.getSubscriberId().equals(subscriberId)) {
            throw new RuntimeException("Session does not match device or subscriber");
        }

        List<ActivePlayback> playbacks = repository.findByKeySubscriberId(subscriberId);

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

        boolean requesterActive = playbacks.stream()
                .anyMatch(p -> p.getSessionId().equals(sessionId));

        return FetchDevicesResponse.builder()
                .sessionId(sessionId)
                .status(requesterActive ? "ACTIVE" : "FORCE_STOP")
                .requestedBySessionId(sessionId)
                .requestedByDeviceId(sessionLookup.getDeviceId())
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
        app_version,
        content_id,
        content_type,
        device_type,
        ip,
        last_seen,
        os,
        playback_token,
        session_id,
        status,
        x_stream_id
    ) VALUES (?, ?, ?, ?, ?, ?, ?, toTimestamp(now()), ?, ?, ?, ?, ?)
    USING TTL ?
    """;

        return cassandraTemplate.getCqlOperations().execute(
                cql,
                existing.getKey().getSubscriberId(),
                existing.getKey().getDeviceId(),
                existing.getAppVersion(),
                existing.getContentId(),
                existing.getContentType(),
                existing.getDeviceType(),
                existing.getIp(),
                existing.getOs(),
                existing.getPlaybackToken(),
                existing.getSessionId(),
                "FORCE_STOP",          // status
                existing.getXStreamId(),
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

