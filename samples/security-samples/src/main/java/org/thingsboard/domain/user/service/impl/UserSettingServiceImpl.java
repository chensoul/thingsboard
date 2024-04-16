package org.thingsboard.domain.user.service.impl;

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
import static org.thingsboard.common.CacheConstants.USER_SETTINGS_CACHE;
import org.thingsboard.common.exception.DataValidationException;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.validation.ConstraintValidator;
import org.thingsboard.domain.user.model.UserSetting;
import org.thingsboard.domain.user.model.UserSettingType;
import org.thingsboard.domain.user.persistence.UserSettingDao;
import org.thingsboard.domain.user.service.UserSettingService;

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
	@CacheEvict(cacheNames = USER_SETTINGS_CACHE, key = "'userSetting'")
	public UserSetting updateUserSetting(Long userId, UserSettingType type, JsonNode settings) {
		UserSetting oldSettings = userSettingDao.findByUserIdAndType(userId, type);
		JsonNode oldSettingsJson = oldSettings != null ? oldSettings.getExtra() : JacksonUtil.newObjectNode();

		UserSetting newUserSetting = new UserSetting();
		newUserSetting.setUserId(userId);
		newUserSetting.setType(type);
		newUserSetting.setExtra(update(oldSettingsJson, settings));

		return doSaveUserSetting(newUserSetting);
	}

	@Override
	@CacheEvict(cacheNames = USER_SETTINGS_CACHE, key = "'userSetting'")
	public UserSetting saveUserSetting(UserSetting userSetting) {
		return doSaveUserSetting(userSetting);
	}

	@Override
	@Cacheable(cacheNames = USER_SETTINGS_CACHE, key = "'userSetting'")
	public UserSetting findUserSetting(Long userId, UserSettingType type) {
		return userSettingDao.findByUserIdAndType(userId, type);
	}

	@Override
	@CacheEvict(cacheNames = USER_SETTINGS_CACHE, key = "'userSetting'")
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
