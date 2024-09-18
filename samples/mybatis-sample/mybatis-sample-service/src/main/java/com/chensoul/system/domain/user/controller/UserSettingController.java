package com.chensoul.system.domain.user.controller;

import static com.chensoul.system.ControllerConstants.USER_SETTING_ID;
import com.chensoul.system.domain.audit.domain.ActionType;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.infrastructure.common.BaseController;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getUserId;
import com.chensoul.system.user.domain.UserSetting;
import com.chensoul.util.RestResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequestMapping("/api/userSettings")
@RestController
@RequiredArgsConstructor
public class UserSettingController extends BaseController {

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    @PostMapping
    public RestResponse<UserSetting> saveUserSetting(@RequestBody UserSetting userSetting) {
        UserSetting old = checkUserSettingId(userSetting.getId());
        return RestResponse.ok((UserSetting) auditLogService.doAndLog(userSetting, old, EntityType.USER_SETTING, ActionType.ADD,
            t -> userSettingService.saveUserSetting(userSetting)));
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    @DeleteMapping(value = "/{userSettingId}")
    public RestResponse<Void> deleteUserSetting(@PathVariable(USER_SETTING_ID) Long userSettingId) {
        UserSetting old = checkUserSettingId(userSettingId);
        auditLogService.doAndLog(old, EntityType.USER_SETTING, ActionType.DELETE,
            t -> userSettingService.deleteUserSetting(old));
        return RestResponse.ok();
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    @GetMapping
    public RestResponse<List<UserSetting>> findUserSettingByUserId() {
        return RestResponse.ok(userSettingService.findUserSettingByUserId(getUserId()));
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    @GetMapping(value = "/{userSettingId}")
    public RestResponse<UserSetting> findUserSettingById(@PathVariable(USER_SETTING_ID) Long userSettingId) {
        return RestResponse.ok(checkUserSettingId(userSettingId));
    }
}
