/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chensoul.system.infrastructure.security.util;

import com.chensoul.system.user.domain.User;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SecurityUser extends User {
    private static final long serialVersionUID = -797397440703066079L;
    private Collection<GrantedAuthority> authorities;

    @Getter
    @Setter
    private boolean enabled;

    @Getter
    @Setter
    private UserPrincipal userPrincipal;

    @Getter
    @Setter
    private String sessionId;

    public SecurityUser() {
        super();
    }

    public SecurityUser(User user, boolean enabled, UserPrincipal userPrincipal) {
        BeanUtils.copyProperties(user, this);
        this.enabled = enabled;
        this.userPrincipal = userPrincipal;
        this.sessionId = UUID.randomUUID().toString();
    }

    public Collection<GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = Stream.of(SecurityUser.this.getAuthority())
                .map(authority -> new SimpleGrantedAuthority(authority.name()))
                .collect(Collectors.toList());
        }
        return authorities;
    }
}
