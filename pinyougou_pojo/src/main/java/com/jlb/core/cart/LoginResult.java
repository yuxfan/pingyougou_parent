package com.jlb.core.cart;

import com.jlb.core.vo.Cart;

import java.io.Serializable;
import java.util.List;

//可以返回是否成功，登录用户名以及返回的结果对象
public class LoginResult implements Serializable {

    private boolean success;  //是否成功

    private String loginname; //用户名

    private Object data; //返回的结果对象

    public LoginResult() {
    }

    public LoginResult(boolean success, String loginname, Object data) {
        this.success = success;
        this.loginname = loginname;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
