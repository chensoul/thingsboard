package com.chensoul.system.user.domain;

import com.chensoul.data.model.BaseDataWithExtra;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserSetting extends BaseDataWithExtra<Long> {
    private static final long serialVersionUID = 2628320657987010348L;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "类型不能为空")
    private UserSettingType type;

}
