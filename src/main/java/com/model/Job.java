package com.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 就业
 *
 * @author Clrvn
 */
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

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", year='" + year + '\'' +
                ", stuNo='" + stuNo + '\'' +
                ", sex='" + sex + '\'' +
                ", afterGraduation='" + afterGraduation + '\'' +
                ", realCompany='" + realCompany + '\'' +
                ", companyLocation='" + companyLocation + '\'' +
                ", companyUnder='" + companyUnder + '\'' +
                ", companyNature='" + companyNature + '\'' +
                ", companyType='" + companyType + '\'' +
                ", industryNature='" + industryNature + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(id, job.id) &&
                Objects.equals(year, job.year) &&
                Objects.equals(stuNo, job.stuNo) &&
                Objects.equals(sex, job.sex) &&
                Objects.equals(afterGraduation, job.afterGraduation) &&
                Objects.equals(realCompany, job.realCompany) &&
                Objects.equals(companyLocation, job.companyLocation) &&
                Objects.equals(companyUnder, job.companyUnder) &&
                Objects.equals(companyNature, job.companyNature) &&
                Objects.equals(companyType, job.companyType) &&
                Objects.equals(industryNature, job.industryNature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, year, stuNo, sex, afterGraduation, realCompany, companyLocation, companyUnder, companyNature, companyType, industryNature);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getStuNo() {
        return stuNo;
    }

    public void setStuNo(String stuNo) {
        this.stuNo = stuNo;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAfterGraduation() {
        return afterGraduation;
    }

    public void setAfterGraduation(String afterGraduation) {
        this.afterGraduation = afterGraduation;
    }

    public String getRealCompany() {
        return realCompany;
    }

    public void setRealCompany(String realCompany) {
        this.realCompany = realCompany;
    }

    public String getCompanyLocation() {
        return companyLocation;
    }

    public void setCompanyLocation(String companyLocation) {
        this.companyLocation = companyLocation;
    }

    public String getCompanyUnder() {
        return companyUnder;
    }

    public void setCompanyUnder(String companyUnder) {
        this.companyUnder = companyUnder;
    }

    public String getCompanyNature() {
        return companyNature;
    }

    public void setCompanyNature(String companyNature) {
        this.companyNature = companyNature;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getIndustryNature() {
        return industryNature;
    }

    public void setIndustryNature(String industryNature) {
        this.industryNature = industryNature;
    }


}
