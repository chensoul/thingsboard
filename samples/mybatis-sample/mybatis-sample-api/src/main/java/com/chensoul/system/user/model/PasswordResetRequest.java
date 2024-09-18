package com.chensoul.system.user.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class PasswordResetRequest implements Serializable {

    private String resetToken;
    private String password;
}
