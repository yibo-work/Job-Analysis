package com.dao;

import com.model.Job;

import java.util.List;
import java.util.Map;


/**
 * 就业DAO
 *
 * @author Clrvn
 */
public interface JobDao {

    /**
     * 通过id查询单个就业
     */
    Job findById(Integer id);

    /**
     * 通过map查询单个就业
     */
    Job findByMap(Map<String, Object> map);

    /**
     * 查询就业列表
     */
    List<Job> list(Map<String, Object> map);

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

}
