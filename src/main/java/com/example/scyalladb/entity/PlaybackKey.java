package com.example.scyalladb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
@Data
@NoArgsConstructor   // Required by Spring Data
@AllArgsConstructor
@PrimaryKeyClass
public class PlaybackKey {

    @PrimaryKeyColumn(
            name = "subscriber_id",
            type = PrimaryKeyType.PARTITIONED
    )
    private String subscriberId;

    @PrimaryKeyColumn(
            name = "device_id",
            type = PrimaryKeyType.CLUSTERED
    )
    private String deviceId;
}
