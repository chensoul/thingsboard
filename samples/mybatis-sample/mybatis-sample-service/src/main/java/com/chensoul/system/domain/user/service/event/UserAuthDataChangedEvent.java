package com.chensoul.system.domain.user.service.event;

import java.io.Serializable;

public abstract class UserAuthDataChangedEvent implements Serializable {
    public abstract String getId();

    public abstract long getTs();
}
