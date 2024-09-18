package com.chensoul.system.domain.notification.channel.mail;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Email {

  private final String from;
  private final String to;
  private final String cc;
  private final String bcc;
  private final String subject;
  private final String body;
  private final Map<String, String> images;
  private final boolean html;

}
