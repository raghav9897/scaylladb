package com.example.scyalladb.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
@Data
@Table("active_playback")
@Builder
public class ActivePlayback {


    @PrimaryKey
    private PlaybackKey key;

    @Column("session_id")
    private String sessionId;

    @Column("device_type")
    private String deviceType;

    @Column("playback_token")
    private String playbackToken;

    @Column("app_version")
    private String appVersion;

    @Column("os")
    private String os;

    @Column("ip")
    private String ip;

    @Column("status")
    private String status;

    @Column("last_seen")
    private Instant lastSeen;

    @Column("content_type")
    private String contentType;

    @Column("content_id")
    private String contentId;

    @Column("x_stream_id")
    private String xStreamId;
}
