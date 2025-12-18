package com.example.scyalladb.repository;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PlaybackTokenRepository {

    private final CassandraTemplate template;

    public PlaybackTokenRepository(CassandraTemplate template) {
        this.template = template;
    }

    public boolean tryAcquire(
            String subscriberId,
            int tokenId,
            String deviceId,
            int ttl
    ) {
        ResultSet rs = template.getCqlOperations().queryForResultSet("""
            INSERT INTO playback_tokens (subscriber_id, token_id, device_id)
            VALUES (?, ?, ?)
            USING TTL ?
            IF NOT EXISTS
        """, subscriberId, tokenId, deviceId, ttl);

        return rs.wasApplied();
    }

    public void release(String subscriberId, int tokenId) {
        template.getCqlOperations().execute("""
            DELETE FROM playback_tokens
            WHERE subscriber_id = ?
            AND token_id = ?
        """, subscriberId, tokenId);
    }
}
