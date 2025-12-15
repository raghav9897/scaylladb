package com.example.scyalladb.config;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CassandraConfig {

    @Bean
    public CqlSessionBuilderCustomizer scyllaCustomizer() {
        return builder -> builder
                .withLocalDatacenter("datacenter1")
                .withKeyspace(CqlIdentifier.fromCql("playback"));
    }

}
