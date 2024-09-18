package com.chensoul.system.user.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class PasswordResetEmailRequest implements Serializable {

    private String email;
}
