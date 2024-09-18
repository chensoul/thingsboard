package com.chensoul.system.domain.usage;

import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getCurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class UsageInfoController {
    @Autowired
    private UsageInfoService usageInfoService;

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/usage", method = RequestMethod.GET)
    public UsageInfo getTenantUsageInfo() {
        return usageInfoService.getUsageInfo(getCurrentUser().getTenantId());
    }
}
