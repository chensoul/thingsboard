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
package com.chensoul.system.domain.notification.domain.template;

import com.chensoul.data.validation.NoXss;
import com.chensoul.system.domain.notification.domain.HasSubject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WebNotificationDeliveryTemplate extends NotificationDeliveryTemplate implements HasSubject {

    @NoXss(fieldName = "web notification subject")
    @Length(max = 150, message = "cannot be longer than 150 chars")
    @NotEmpty
    private String subject;
    private JsonNode config;

    private final List<TemplateValue> templateValues = Arrays.asList(
        TemplateValue.of(this::getBody, this::setBody),
        TemplateValue.of(this::getSubject, this::setSubject),
        TemplateValue.of(this::getButtonText, this::setButtonText),
        TemplateValue.of(this::getButtonLink, this::setButtonLink)
    );

    public WebNotificationDeliveryTemplate(WebNotificationDeliveryTemplate other) {
        super(other);
        this.subject = other.subject;
        this.config = other.config != null ? other.config.deepCopy() : null;
    }

    @Length(max = 250, message = "cannot be longer than 250 chars")
    @Override
    public String getBody() {
        return super.getBody();
    }

    @NoXss(fieldName = "web notification button text")
    @Length(max = 50, message = "cannot be longer than 50 chars")
    @JsonIgnore
    public String getButtonText() {
        return getButtonConfigProperty("text");
    }

    @JsonIgnore
    public void setButtonText(String buttonText) {
        getButtonConfig().ifPresent(buttonConfig -> {
            buttonConfig.set("text", new TextNode(buttonText));
        });
    }

    @NoXss(fieldName = "web notification button link")
    @Length(max = 300, message = "cannot be longer than 300 chars")
    @JsonIgnore
    public String getButtonLink() {
        return getButtonConfigProperty("link");
    }

    @JsonIgnore
    public void setButtonLink(String buttonLink) {
        getButtonConfig().ifPresent(buttonConfig -> {
            buttonConfig.set("link", new TextNode(buttonLink));
        });
    }

    private String getButtonConfigProperty(String property) {
        return getButtonConfig()
            .map(buttonConfig -> buttonConfig.get(property))
            .filter(JsonNode::isTextual)
            .map(JsonNode::asText).orElse(null);
    }

    private Optional<ObjectNode> getButtonConfig() {
        return Optional.ofNullable(config)
            .map(config -> config.get("actionButtonConfig")).filter(JsonNode::isObject)
            .map(config -> (ObjectNode) config);
    }

    @Override
    public NotificationDeliveryMethod getDeliveryMethod() {
        return NotificationDeliveryMethod.WEB;
    }

    @Override
    public WebNotificationDeliveryTemplate copy() {
        return new WebNotificationDeliveryTemplate(this);
    }

}
