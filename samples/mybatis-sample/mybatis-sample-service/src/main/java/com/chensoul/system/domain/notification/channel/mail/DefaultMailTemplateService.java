/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
