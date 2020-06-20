package com.service;

import com.github.pagehelper.PageInfo;
import com.model.Job;
import com.utils.RequestParamsUtil;
import com.vo.ResultVO;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 就业Service
 *
 * @author Clrvn
 */
public interface JobService {

    /**
     * 查询就业页面
     *
     * @return 分页就业数据
     */
    PageInfo<Job> page(RequestParamsUtil requestParamsUtil);

    /**
     * 查询就业列表
     */
    List<Job> list(Map<String, Object> map);

    /**
     * 通过id查询单个就业
     */
    Job findById(Integer id);

    /**
     * 通过map查询单个就业
     */
    Job findByMap(Map<String, Object> map);

    /**
     * 新增就业
     */
    int save(Job job);

    /**
     * 修改就业
     */
    int update(Job job);

    /**
     * 删除就业
     */
    int deleteById(Integer id);

    /**
     * 导入数据
     */
    ResultVO importData(File targetFile) throws Exception;

    /**
     * 清空就业表
     */
    void truncate();

    /**
     * 分析数据
     */
    ResultVO getAnalysisData(Map<String, Object> parameters);
}
