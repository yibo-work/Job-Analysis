package com.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户
 *
 * @author Clrvn
 */
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Integer id;

    /**
     * 用户名
     */
    private String name;

    /**
     * 密码
     */
    private String password;


}
