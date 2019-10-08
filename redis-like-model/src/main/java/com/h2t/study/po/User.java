package com.h2t.study.po;

import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 用户实体
 *
 * @author hetiantian
 * @since 2019-10-08
 */
public class User extends BasePO {
    /**
     * 用户名
     */
    @TableField(value = "username", exist = true)
    private String username;

    /**
     * 密码
     */
    @TableField(value = "password", exist = true)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
