package com.chensoul.system.domain.notification.channel.mail;

import com.chensoul.json.JacksonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultMailTemplateService implements MailTemplateService {

  private JsonNode mailConfigTemplates;

  @PostConstruct
  private void postConstruct() throws IOException {
    mailConfigTemplates = JacksonUtils.readTree(new ClassPathResource("/templates/mail_config_templates.json").getFile());
  }

  @Override
  public JsonNode findAllMailConfigTemplates() {
    return mailConfigTemplates;
  }
}
