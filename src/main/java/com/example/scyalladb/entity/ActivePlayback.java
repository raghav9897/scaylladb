package com.example.scyalladb.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
@Data
@Table("active_playback")
public class ActivePlayback {


    @PrimaryKey
    private PlaybackKey key;

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

    @Column("last_seen")
    private Instant lastSeen;
}
