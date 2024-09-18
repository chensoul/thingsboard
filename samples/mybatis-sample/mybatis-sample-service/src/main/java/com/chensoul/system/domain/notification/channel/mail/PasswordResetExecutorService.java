package com.chensoul.system.domain.notification.channel.mail;

import com.chensoul.util.concurrent.AbstractListeningExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetExecutorService extends AbstractListeningExecutor {

  @Value("${actors.rule.mail_password_reset_thread_pool_size:10}")
  private int mailExecutorThreadPoolSize;

  @Override
  protected int getThreadPollSize() {
    return mailExecutorThreadPoolSize;
  }

}
