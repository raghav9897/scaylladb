package com.example.scyalladb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("playback_session_lookup")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaybackSessionLookup {

    @PrimaryKey
    @Column("session_id")
    private String sessionId;

    private String subscriberId;
    private String deviceId;
}
