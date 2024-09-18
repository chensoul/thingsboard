package com.chensoul.system.user.model;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordChangeRequest {
    @NotBlank(message = "Current password should be specified")
    private String currentPassword;

    @NotBlank(message = "New password should be specified")
    private String newPassword;

}
