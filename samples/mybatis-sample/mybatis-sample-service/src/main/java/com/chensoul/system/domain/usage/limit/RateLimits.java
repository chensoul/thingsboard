package com.chensoul.system.domain.usage.limit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.local.LocalBucket;
import io.github.bucket4j.local.LocalBucketBuilder;
import java.time.Duration;
import lombok.Getter;

/**
 * TODO
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
public class RateLimits {
    private final LocalBucket bucket;

    @Getter
    private final String configuration;

    public RateLimits(String limitsConfiguration) {
        this(limitsConfiguration, false);
    }

    public RateLimits(String limitsConfiguration, boolean refillIntervally) {
        LocalBucketBuilder builder = Bucket.builder();
        boolean initialized = false;
        for (String limitSrc : limitsConfiguration.split(",")) {
            long capacity = Long.parseLong(limitSrc.split(":")[0]);
            long duration = Long.parseLong(limitSrc.split(":")[1]);
            Refill refill = refillIntervally ? Refill.intervally(capacity, Duration.ofSeconds(duration)) : Refill.greedy(capacity, Duration.ofSeconds(duration));
            builder.addLimit(Bandwidth.classic(capacity, refill));
            initialized = true;
        }
        if (initialized) {
            bucket = builder.build();
        } else {
            throw new IllegalArgumentException("Failed to parse rate limits configuration: " + limitsConfiguration);
        }
        this.configuration = limitsConfiguration;
    }

    public boolean tryConsume() {
        return bucket.tryConsume(1);
    }

    public boolean tryConsume(long number) {
        return bucket.tryConsume(number);
    }

}
