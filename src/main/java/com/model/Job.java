package com.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 就业
 *
 * @author Clrvn
 */
@Data
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Integer id;

    /**
     * 年度
     */
    private String year;

    /**
     * 学号
     */
    private String stuNo;

    /**
     * 性别
     */
    private String sex;

    /**
     * 毕业去向
     */
    private String afterGraduation;

    /**
     * 实际单位
     */
    private String realCompany;

    /**
     * 单位所在地
     */
    private String companyLocation;

    /**
     * 单位隶属
     */
    private String companyUnder;

    /**
     * 单位性质
     */
    private String companyNature;

    /**
     * 单位类型
     */
    private String companyType;

    /**
     * 行业性质
     */
    private String industryNature;


}
