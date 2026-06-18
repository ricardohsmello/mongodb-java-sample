package com.example.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.connection.ConnectionPoolSettings;
import io.quarkus.mongodb.runtime.MongoClientCustomizer;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MongoClientConfig implements MongoClientCustomizer {

    @ConfigProperty(name = "mongodb.pool.max-size", defaultValue = "20")
    int maxSize;

    @ConfigProperty(name = "mongodb.pool.min-size", defaultValue = "5")
    int minSize;

    @ConfigProperty(name = "mongodb.pool.max-wait-time-ms", defaultValue = "5000")
    long maxWaitTimeMs;

    @ConfigProperty(name = "mongodb.pool.max-connection-idle-time-ms", defaultValue = "60000")
    long maxConnectionIdleTimeMs;

    @ConfigProperty(name = "mongodb.pool.max-connection-life-time-ms", defaultValue = "300000")
    long maxConnectionLifeTimeMs;

    @Override
    public MongoClientSettings.Builder customize(MongoClientSettings.Builder builder) {
        return builder
                .applicationName("devrel-java-quarkus")
                .readPreference(ReadPreference.secondary())
                .applyToConnectionPoolSettings(pool -> pool
                        .maxSize(maxSize)
                        .minSize(minSize)
                        .maxWaitTime(maxWaitTimeMs, TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(maxConnectionIdleTimeMs, TimeUnit.MILLISECONDS)
                        .maxConnectionLifeTime(maxConnectionLifeTimeMs, TimeUnit.MILLISECONDS)
                );
    }

}
