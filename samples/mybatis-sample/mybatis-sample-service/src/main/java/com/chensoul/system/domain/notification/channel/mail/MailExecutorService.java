package com.chensoul.system.domain.notification.channel.mail;

import com.chensoul.util.concurrent.AbstractListeningExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailExecutorService extends AbstractListeningExecutor {

  @Value("${actors.rule.mail_thread_pool_size:4}")
  private int mailExecutorThreadPoolSize;

  @Override
  protected int getThreadPollSize() {
    return mailExecutorThreadPoolSize;
  }

}
