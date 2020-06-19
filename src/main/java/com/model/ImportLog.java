package com.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 导入记录
 *
 * @author Clrvn
 */
@Data
public class ImportLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键自增id
     */
    private Integer id;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 性别
     */
    private String sex;

    /**
     * 学院
     */
    private String schoolCode;

    /**
     * 就业金额
     */
    private BigDecimal money;

    /**
     * 就业时间
     */
    private Date time;

    /**
     * 就业地点
     */
    private String station;

    /**
     * 导入时间
     */
    private Date createTime;


}
