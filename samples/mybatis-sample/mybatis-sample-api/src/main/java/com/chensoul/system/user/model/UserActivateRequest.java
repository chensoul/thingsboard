package com.chensoul.system.user.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class UserActivateRequest implements Serializable {
    private String activateToken;
    private String password;
}
