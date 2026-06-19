package com.example.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import io.quarkus.mongodb.runtime.MongoClientCustomizer;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MongoClientConfig implements MongoClientCustomizer {

    @ConfigProperty(name = "mongodb.pool.max-size", defaultValue = "100")
    int maxSize;

    @ConfigProperty(name = "mongodb.pool.min-size", defaultValue = "0")
    int minSize;

    @ConfigProperty(name = "mongodb.pool.max-connection-idle-time-ms", defaultValue = "0")
    long maxConnectionIdleTimeMs;

    @ConfigProperty(name = "mongodb.pool.max-connection-life-time-ms", defaultValue = "0")
    long maxConnectionLifeTimeMs;

    // CSOT: 0 = disabled (driver default). Set via mongodb.timeout-ms to enable.
    @ConfigProperty(name = "mongodb.timeout-ms", defaultValue = "0")
    long timeoutMs;

    @ConfigProperty(name = "mongodb.read-preference", defaultValue = "primaryPreferred")
    String readPreference;

    @Override
    public MongoClientSettings.Builder customize(MongoClientSettings.Builder builder) {
        builder
                .applicationName("devrel-tutorial-java-quarkus-library")
                .readPreference(ReadPreference.valueOf(readPreference))
                .applyToConnectionPoolSettings(pool -> pool
                        .maxSize(maxSize)
                        .minSize(minSize)
                        .maxConnectionIdleTime(maxConnectionIdleTimeMs, TimeUnit.MILLISECONDS)
                        .maxConnectionLifeTime(maxConnectionLifeTimeMs, TimeUnit.MILLISECONDS)
                );

        if (timeoutMs > 0) {
            builder.timeout(timeoutMs, TimeUnit.MILLISECONDS);
        }

        return builder;
    }

}
