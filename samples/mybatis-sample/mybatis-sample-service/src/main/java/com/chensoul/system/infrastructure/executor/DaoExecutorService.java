package com.chensoul.system.infrastructure.executor;

import com.chensoul.util.concurrent.AbstractListeningExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DaoExecutorService extends AbstractListeningExecutor {

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int poolSize;

    @Override
    protected int getThreadPollSize() {
        return poolSize;
    }

}
