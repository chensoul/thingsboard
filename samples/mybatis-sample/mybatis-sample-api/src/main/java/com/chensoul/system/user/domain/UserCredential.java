package com.chensoul.system.user.domain;

import com.chensoul.data.model.BaseDataWithExtra;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserCredential extends BaseDataWithExtra<Long> {
    private static final long serialVersionUID = -2108436378880529163L;

    private Long userId;
    private boolean enabled;
    private String password;
    private String activateToken;
    private String resetToken;
}
