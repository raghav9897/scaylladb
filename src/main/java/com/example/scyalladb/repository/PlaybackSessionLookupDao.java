package com.example.scyalladb.repository;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.example.scyalladb.entity.ActivePlayback;
import com.example.scyalladb.entity.PlaybackSessionLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlaybackSessionLookupDao {

    private final CassandraTemplate cassandraTemplate;

    public void saveWithTtl(
            PlaybackSessionLookup lookup,
            int ttlSeconds
    ) {
        cassandraTemplate.insert(
                lookup,
                InsertOptions.builder()
                        .ttl(ttlSeconds)
                        .build()
        );
    }

    public Optional<PlaybackSessionLookup> findBySessionId(String sessionId) {
        SimpleStatement select = QueryBuilder.selectFrom("playback_session_lookup").all()
                .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                .build();

        PlaybackSessionLookup result = cassandraTemplate.selectOne(select, PlaybackSessionLookup.class);
        return Optional.ofNullable(result);
    }


    public Optional<ActivePlayback> findBySessionIdActivePlayback(String sessionId) {
        return Optional.ofNullable(cassandraTemplate.selectOne(
                Query.query(Criteria.where("session_id").is(sessionId)),
                ActivePlayback.class
        ));
    }
}

