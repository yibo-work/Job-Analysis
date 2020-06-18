package com.service.impl;

import com.dao.JobDao;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.model.Job;
import com.service.JobService;
import com.utils.RequestParamsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 就业ServiceImpl
 *
 * @author Clrvn
 */
@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobDao jobDao;

    /**
     * 查询就业页面
     *
     * @return 分页就业数据
     */
    @Override
    public PageInfo<Job> page(RequestParamsUtil requestParamsUtil) {
        PageHelper.startPage(requestParamsUtil.getPageNo(), requestParamsUtil.getPageSize());
        return new PageInfo<>(jobDao.list(requestParamsUtil.getParameters()));
    }

    /**
     * 查询就业列表
     */
    @Override
    public List<Job> list(Map<String, Object> map) {
        return jobDao.list(map);
    }


    /**
     * 通过id查询单个就业
     */
    @Override
    public Job findById(Integer id) {
        return jobDao.findById(id);
    }

    /**
     * 通过map查询单个就业
     */
    @Override
    public Job findByMap(Map<String, Object> map) {
        return jobDao.findByMap(map);
    }

    /**
     * 新增就业
     */
    @Override
    public int save(Job job) {
        return jobDao.save(job);
    }

    /**
     * 修改就业
     */
    @Override
    public int update(Job job) {
        return jobDao.update(job);
    }

    /**
     * 删除就业
     */
    @Override
    public int deleteById(Integer id) {
        return jobDao.deleteById(id);
    }

}
