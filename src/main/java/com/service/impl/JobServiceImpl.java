package com.service.impl;

import com.dao.JobDao;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.model.Job;
import com.service.JobService;
import com.utils.RequestParamsUtil;
import com.utils.XLSConvertCSVUtil;
import com.utils.XLSXCovertCSVUtil;
import com.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
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

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public ResultVO importData(File targetFile) throws Exception {

        ResultVO resultVO = new ResultVO();

        String originalFilename = targetFile.getName();
        // 解析excel的第一个sheet页，导入全部资质的数据
        int columnsForRead = Job.class.getDeclaredFields().length - 1;
        List<List<String>> list;
        if (originalFilename.toLowerCase().endsWith(".xlsx")) {
            list = XLSXCovertCSVUtil.readXLSX(targetFile, 1, 2, columnsForRead);
        } else {
            list = XLSConvertCSVUtil.readXLS(targetFile, 1, 2, columnsForRead);
        }

        List<Job> jobList = new ArrayList<>(list.size());
        for (List<String> strings : list) {
            Job job = new Job();

            job.setYear(strings.get(0));
            job.setStuNo(strings.get(1));
            job.setSex(strings.get(2));
            job.setAfterGraduation(strings.get(3));
            job.setRealCompany(strings.get(4));
            job.setCompanyLocation(strings.get(5));
            job.setCompanyUnder(strings.get(6));
            job.setCompanyNature(strings.get(7));
            job.setCompanyType(strings.get(8));
            job.setIndustryNature(strings.get(9));

            jobList.add(job);
        }

        if (jobList.isEmpty()) {
            resultVO.setCode(500);
            resultVO.setMsg("导入失败,无数据！");
        } else {
            jobDao.bitchSave(jobList);
            resultVO.setCode(200);
            resultVO.setMsg("导入成功！");
        }
        return resultVO;
    }

    @Override
    public void truncate() {
        jobDao.truncate ();
    }

}
