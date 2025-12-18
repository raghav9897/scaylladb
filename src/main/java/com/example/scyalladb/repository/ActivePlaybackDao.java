package com.example.scyalladb.repository;

import com.example.scyalladb.entity.ActivePlayback;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.UpdateOptions;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ActivePlaybackDao {

    private final CassandraTemplate cassandraTemplate;

    public void saveWithTtl(ActivePlayback playback, int ttlSeconds) {
        cassandraTemplate.insert(
                playback,
                InsertOptions.builder()
                        .ttl(ttlSeconds)
                        .build()
        );
    }

    public void updateWithTtl(ActivePlayback playback, int ttlSeconds) {
        cassandraTemplate.update(
                playback,
                UpdateOptions.builder()
                        .ttl(ttlSeconds)
                        .build()
        );
    }
}
