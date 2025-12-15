package com.example.scyalladb.service;

import com.example.scyalladb.dto.FetchDeviceDTO;
import com.example.scyalladb.dto.RegisterPlaybackRequest;
import com.example.scyalladb.entity.ActivePlayback;
import com.example.scyalladb.repository.ActivePlaybackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaybackService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PlaybackService.class);
    private final CassandraTemplate cassandraTemplate;
    private final ActivePlaybackRepository repository;

    private static final int TTL_SECONDS = 100;
    private static final int CONCURRENCY = 2;

    // Register Playback
    public boolean register(RegisterPlaybackRequest req) {

         int deviceCount = fetchActiveDevices( req.getSubscriberId()).size();

         if (deviceCount>=CONCURRENCY){
             return false;
         }

         return registerQuery(req);
    }

    // Heartbeat
    public boolean heartbeat(String subscriberId, String deviceId) {

        ActivePlayback existing = repository.findByKeySubscriberId(subscriberId).stream()
                .filter(x -> x.getKey().getDeviceId().equals(deviceId))
                .findFirst()
                .orElse(null);

        if (existing == null) return false;

       return heartbeatQuery(existing);

    }

    // Fetch Active Devices
    public List<FetchDeviceDTO> fetchActiveDevices(String subscriberId) {
        log.info("Fetching active devices for subscriber={}", subscriberId);
        List<ActivePlayback> devices = repository.findByKeySubscriberId(subscriberId);
        log.info("Found {} active devices for subscriber={}", devices.size(), subscriberId);

        devices.forEach(x->log.info("{}, ={},={}",x.getKey().getDeviceId(),x.getLastSeen(),x.getDeviceType()));
        List<FetchDeviceDTO> active = devices.stream()
                                              .map(x->new FetchDeviceDTO(x.getKey().getDeviceId(),x.getLastSeen(),x.getDeviceType()))
                .collect(Collectors.toUnmodifiableList());
        active.forEach(x->log.info("{}, ={},={}",x.getDeviceId(),x.getLastSeen(),x.getDeviceType()));
        return active;
    }

    // Destroy Playback
    public boolean destroy(String subscriberId, String deviceId) {

        ActivePlayback existing = repository.findByKeySubscriberIdAndKeyDeviceId(subscriberId,deviceId).stream().findFirst()
                .orElse(null);

        if (existing == null) return false;



       return destroyQuery(existing);
    }

    public boolean registerQuery(RegisterPlaybackRequest req){
        String cql = """
            INSERT INTO active_playback (
                subscriber_id, device_id, device_type,
                playback_token, app_version, os, ip, last_seen
            ) VALUES (?, ?, ?, ?, ?, ?, ?, toTimestamp(now()))
            USING TTL ?
        """;

        return cassandraTemplate.getCqlOperations().execute(
                cql,
                req.getSubscriberId(),
                req.getDeviceId(),
                req.getDeviceType(),
                req.getPlaybackToken(),
                req.getAppVersion(),
                req.getOs(),
                req.getIp(),
                TTL_SECONDS
        );
    }

    public boolean heartbeatQuery( ActivePlayback existing){
        String cql = """
    INSERT INTO active_playback (
        subscriber_id, device_id, device_type,
        playback_token, app_version, os, ip, last_seen
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    USING TTL ?
""";

        return cassandraTemplate.getCqlOperations().execute(
                cql,
                existing.getKey().getSubscriberId(),
                existing.getKey().getDeviceId(),
                existing.getDeviceType(),
                existing.getPlaybackToken(),
                existing.getAppVersion(),
                existing.getOs(),
                existing.getIp(),
                Instant.now(), // update last_seen if you want
                TTL_SECONDS
        );
    }

    public boolean destroyQuery(ActivePlayback existing){
        return cassandraTemplate.getCqlOperations().execute(
                "DELETE FROM active_playback WHERE subscriber_id = ? AND device_id = ?",
                existing.getKey().getSubscriberId(),
                existing.getKey().getDeviceId()
        );
    }
}

