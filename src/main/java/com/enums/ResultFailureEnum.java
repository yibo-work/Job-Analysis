package com.enums;

import lombok.Getter;

@Getter
public enum ResultFailureEnum {
    /**
     * 返回错误信息枚举类
     */
    LOGIN_ERROR(1, "登录失败"),
    IMPORT_ERROR(2, "导入失败"),
    REGISTER_ERROR(3, "添加用户失败，有重复的工号"),
    ;

    private final Integer code;

    private final String msg;

    ResultFailureEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
