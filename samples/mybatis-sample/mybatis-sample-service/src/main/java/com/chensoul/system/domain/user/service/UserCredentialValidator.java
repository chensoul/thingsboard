package com.chensoul.system.domain.user.service;

import com.chensoul.exception.BusinessException;
import com.chensoul.system.domain.user.mybatis.UserCredentialDao;
import com.chensoul.system.user.domain.User;
import com.chensoul.system.user.domain.UserCredential;
import com.chensoul.validation.DataValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class UserCredentialValidator extends DataValidator<UserCredential> {

    @Autowired
    private UserCredentialDao userCredentialDao;

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    protected void validateCreate(UserCredential userCredential) {
        throw new BusinessException("Creation of new user credential is prohibited.");
    }

    @Override
    protected void validateDataImpl(UserCredential userCredential) {
        if (userCredential.getUserId() == null) {
            throw new BusinessException("User credential should be assigned to user!");
        }
        if (userCredential.isEnabled()) {
            if (StringUtils.isEmpty(userCredential.getPassword())) {
                throw new BusinessException("Enabled user credential should have password!");
            }
            if (StringUtils.isNotEmpty(userCredential.getActivateToken())) {
                throw new BusinessException("Enabled user credential can't have activate token!");
            }
        }
        UserCredential existingUserCredentialEntity = userCredentialDao.findById(userCredential.getId());
        if (existingUserCredentialEntity == null) {
            throw new BusinessException("Unable to update non-existent user credential!");
        }
        User user = userService.findUserById(userCredential.getUserId());
        if (user == null) {
            throw new BusinessException("Can't assign user credential to non-existent user!");
        }
    }
}
