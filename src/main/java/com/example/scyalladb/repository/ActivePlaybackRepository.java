package com.example.scyalladb.repository;

import com.example.scyalladb.entity.ActivePlayback;
import com.example.scyalladb.entity.PlaybackKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivePlaybackRepository
        extends CassandraRepository<ActivePlayback, PlaybackKey> {

    List<ActivePlayback> findByKeySubscriberId(String subscriberId);
    Optional<ActivePlayback> findByKeySubscriberIdAndKeyDeviceId(String subscriberId, String deviceId);

}

