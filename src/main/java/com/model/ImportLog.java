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
     * 消费金额
     */
    private BigDecimal money;

    /**
     * 消费时间
     */
    private Date time;

    /**
     * 消费地点
     */
    private String station;

    /**
     * 导入时间
     */
    private Date createTime;


}
