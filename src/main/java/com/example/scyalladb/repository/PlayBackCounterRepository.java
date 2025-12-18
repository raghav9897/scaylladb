package com.example.scyalladb.repository;


import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Repository;
import com.datastax.oss.driver.api.core.cql.Row;
import org.springframework.data.cassandra.core.cql.CqlTemplate;


@Repository
@RequiredArgsConstructor
public class PlayBackCounterRepository {

    private final CassandraTemplate cassandraTemplate;

    /**
     * Increment active_count if below maxAllowed.
     * Handles missing rows and concurrency safely using LWT.
     */
    public boolean incrementIfAllowed(String subscriberId, int maxConcurrent) {
        String cql = """
        UPDATE playback_counter 
        SET active_count = active_count + 1
        WHERE subscriber_id = ?
        IF active_count < ?
    """;

        // Try incrementing atomically
        boolean applied = cassandraTemplate.getCqlOperations().execute(cql, subscriberId, maxConcurrent);

        // Row might not exist yet
        if (!applied) {
            // Initialize row if missing
            cassandraTemplate.getCqlOperations().execute(
                    "INSERT INTO playback_counter(subscriber_id, active_count) VALUES (?, 0) IF NOT EXISTS",
                    subscriberId
            );

            // Retry the increment
            applied = cassandraTemplate.getCqlOperations().execute(cql, subscriberId, maxConcurrent);

            if (!applied) {
                throw new RuntimeException("Concurrent playback limit exceeded");
            }
        }

        return true;
    }

    public void decrement(String subscriberId) {
        cassandraTemplate.getCqlOperations().execute(
                "UPDATE playback_counter SET active_count = active_count - 1 WHERE subscriber_id = ?",
                subscriberId
        );
    }


}
