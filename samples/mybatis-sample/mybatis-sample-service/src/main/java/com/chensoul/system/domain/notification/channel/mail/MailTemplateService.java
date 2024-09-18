package com.chensoul.system.domain.notification.channel.mail;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public interface MailTemplateService {
  JsonNode findAllMailConfigTemplates() throws IOException;
}
