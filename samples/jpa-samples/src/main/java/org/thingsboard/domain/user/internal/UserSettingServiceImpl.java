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
package org.thingsboard.domain.user.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import static org.thingsboard.common.CacheConstants.USER_SETTING_CACHE;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.validation.ConstraintValidator;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.domain.user.UserSetting;
import org.thingsboard.domain.user.UserSettingService;
import org.thingsboard.domain.user.UserSettingType;
import org.thingsboard.domain.user.internal.persistence.UserSettingDao;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
@RequiredArgsConstructor
public class UserSettingServiceImpl implements UserSettingService {
	private final UserSettingDao userSettingDao;

	@Override
	@CacheEvict(cacheNames = USER_SETTING_CACHE, key = "'userSetting'")
	public UserSetting updateUserSetting(Long userId, UserSettingType type, JsonNode extra) {
		UserSetting oldSettings = userSettingDao.findByUserIdAndType(userId, type);
		JsonNode oldSettingsJson = oldSettings != null ? oldSettings.getExtra() : JacksonUtil.newObjectNode();

		UserSetting newUserSetting = new UserSetting();
		newUserSetting.setUserId(userId);
		newUserSetting.setType(type);
		newUserSetting.setExtra(update(oldSettingsJson, extra));

		return doSaveUserSetting(newUserSetting);
	}

	@Override
	@CacheEvict(cacheNames = USER_SETTING_CACHE, key = "'userSetting'")
	public UserSetting saveUserSetting(UserSetting userSetting) {
		return doSaveUserSetting(userSetting);
	}

	@Override
	@Cacheable(cacheNames = USER_SETTING_CACHE, key = "'userSetting'")
	public UserSetting findUserSetting(Long userId, UserSettingType type) {
		return userSettingDao.findByUserIdAndType(userId, type);
	}

	@Override
	@CacheEvict(cacheNames = USER_SETTING_CACHE, key = "'userSetting'")
	public UserSetting deleteUserSetting(Long userId, UserSettingType type, List<String> jsonPaths) {
		UserSetting userSetting = findUserSetting(userId, type);
		if (userSetting == null) {
			return null;
		}
		DocumentContext dcSettings = JsonPath.parse(userSetting.getExtra().toString());
		for (String s : jsonPaths) {
			dcSettings = dcSettings.delete("$." + s);
		}
		userSetting.setExtra(JacksonUtil.fromString(dcSettings.jsonString(), ObjectNode.class));

		return saveUserSetting(userSetting);
	}

	private UserSetting doSaveUserSetting(UserSetting userSetting) {
		ConstraintValidator.validateFields(userSetting);
		validateJsonKeys(userSetting.getExtra());
		return userSettingDao.save(userSetting);
	}

	private void validateJsonKeys(JsonNode userSetting) {
		Iterator<String> fieldNames = userSetting.fieldNames();
		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			if (fieldName.contains(".") || fieldName.contains(",")) {
				throw new DataValidationException("Json field name should not contain \".\" or \",\" symbols");
			}
		}
	}

	public JsonNode update(JsonNode mainNode, JsonNode updateNode) {
		Iterator<String> fieldNames = updateNode.fieldNames();
		while (fieldNames.hasNext()) {
			String fieldExpression = fieldNames.next();
			String[] fieldPath = fieldExpression.trim().split("\\.");
			var node = (ObjectNode) mainNode;
			for (int i = 0; i < fieldPath.length; i++) {
				var fieldName = fieldPath[i];
				var last = i == (fieldPath.length - 1);
				if (last) {
					node.set(fieldName, updateNode.get(fieldExpression));
				} else {
					if (!node.has(fieldName)) {
						node.set(fieldName, JacksonUtil.newObjectNode());
					}
					node = (ObjectNode) node.get(fieldName);
				}
			}
		}
		return mainNode;
	}

}
