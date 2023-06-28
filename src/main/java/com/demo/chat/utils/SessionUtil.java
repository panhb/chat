package com.demo.chat.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.demo.chat.model.po.UserPO;

/**
 * @author hongbo.pan
 * @date 2023/6/28
 */
public class SessionUtil {

    public static final String USER = "user";

    public static void setUser(UserPO po) {
        StpUtil.getSession().set(USER, po);
    }

    public static UserPO getUser() {
        return StpUtil.getSession().getModel(USER, UserPO.class);
    }
}

