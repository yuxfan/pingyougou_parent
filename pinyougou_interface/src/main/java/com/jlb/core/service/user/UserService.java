package com.jlb.core.service.user;

import com.jlb.core.pojo.user.User;

public interface UserService {

    public void add(User user);

    public void createSmsCode(String phone);

    public boolean checkSmsCode(String phone, String smscode);
}
