/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubboadmin.governance.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.dubboadmin.governance.service.UserService;
import com.alibaba.dubboadmin.registry.common.domain.User;
import com.alibaba.dubboadmin.registry.common.util.Coder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * IBatisUserService
 */
@Component
public class UserServiceImpl extends AbstractService implements UserService {

    @Value("${spring.root.password}")
    private String rootPassword;
    @Value("${spring.guest.password}")
    private String guestPassword;

    public void setRootPassword(String password) {
        this.rootPassword = (password == null ? "" : password);
    }

    public void setGuestPassword(String password) {
        this.guestPassword = (password == null ? "" : password);
    }

    /**
     * 用户登录时： 用户名 + 密码，返回用户信息（包含角色和权限）
     *
     * @param username
     * @return
     */
    @Override
    public User findUser(String username) {
        // guest - guest
        if ("guest".equals(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(Coder.encodeMd5(username + ":" + User.REALM + ":" + guestPassword));
            user.setName(username);
            user.setRole(User.GUEST);
            user.setEnabled(true);
            user.setLocale("zh");
            user.setServicePrivilege("");
            return user;

            // root - root
        } else if ("root".equals(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(Coder.encodeMd5(username + ":" + User.REALM + ":" + rootPassword));
            user.setName(username);
            user.setRole(User.ROOT);
            user.setEnabled(true);
            user.setLocale("zh");
            user.setServicePrivilege("*");
            return user;
        }

        return null;
    }

    @Override
    public List<User> findAllUsers() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, User> findAllUsersMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User findById(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createUser(User user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateUser(User user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void modifyUser(User user) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean updatePassword(User user, String oldPassword) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void resetPassword(User user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void enableUser(User user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disableUser(User user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteUser(User user) {
        // TODO Auto-generated method stub

    }

    public List<User> findUsersByServiceName(String serviceName) {
        // TODO Auto-generated method stub
        return null;
    }

}