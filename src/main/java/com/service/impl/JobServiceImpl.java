package com.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.dao.JobDao;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.model.Job;
import com.service.JobService;
import com.utils.RequestParamsUtil;
import com.utils.ResultVOUtil;
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
import java.util.HashMap;
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
        jobDao.truncate();
    }

    /**
     * 分析数据
     */
    @Override
    public ResultVO getAnalysisData(Map<String, Object> parameters) {

        /*

         * ⭐⭐⭐  总共多少人，男生，女生多少人（数值）
         * ⭐⭐⭐  年度男生，女生多少人（柱状图）
         * ⭐⭐⭐  毕业去向（饼形图）
         * ⭐⭐⭐  年度和毕业去向的关系（折线图）待完成
         * ⭐⭐⭐  性别和行业性质的关系（折线图）待完成
         *
         */
        Map<String, Object> resultMap = new HashMap<>();
        List<Job> jobList = jobDao.list(parameters);

        if (CollectionUtil.isEmpty(jobList)) {
            return ResultVOUtil.failure("无学生就业数据");
        }

        int maleCount = (int) jobList.stream().filter(job -> "男".equals(job.getSex())).count();
        int femaleCount = (int) jobList.stream().filter(job -> "女".equals(job.getSex())).count();

        resultMap.put("maleCount", maleCount);
        resultMap.put("femaleCount", femaleCount);
        resultMap.put("manCount", jobList.size());

        Map<String, Integer> afterGraduationData = new HashMap<>();

        jobList.forEach(job -> {
            String key = job.getAfterGraduation();
            afterGraduationData.merge(key, 1, Integer::sum);
        });

        resultMap.put("afterGraduationData", afterGraduationData);

        Map<String, int[]> yearData = new HashMap<>();

        jobList.forEach(job -> {
            String key = job.getYear();
            int[] val = yearData.get(key);
            if (val == null) {
                val = new int[2];
                if ("男".equals(job.getSex())) {
                    val[0] = val[0] + 1;
                } else if ("女".equals(job.getSex())) {
                    val[1] = val[1] + 1;
                }
                yearData.put(key, val);
            } else {
                if ("男".equals(job.getSex())) {
                    val[0] = val[0] + 1;
                } else if ("女".equals(job.getSex())) {
                    val[1] = val[1] + 1;
                }
                yearData.put(key, val);
            }
        });

        resultMap.put("yearData", yearData);

        Map<String, int[]> yearAfterData = new HashMap<>();

        List<String> afterList = new ArrayList<>(afterGraduationData.keySet());

        jobList.forEach(job -> {
            String key = job.getYear();
            int[] val = yearAfterData.get(key);
            if (val == null) {
                val = new int[afterList.size()];
                for (int i = 0; i < afterList.size(); i++) {
                    if (afterList.get(i).equals(job.getAfterGraduation())) {
                        val[i] = val[i] + 1;
                    }
                }
                yearAfterData.put(key, val);
            } else {

                for (int i = 0; i < afterList.size(); i++) {
                    if (afterList.get(i).equals(job.getAfterGraduation())) {
                        val[i] = val[i] + 1;
                    }
                }
                yearAfterData.put(key, val);
            }
        });

        resultMap.put("afterList", afterList);
        resultMap.put("yearAfterData", yearAfterData);

        List<Integer> maleArr = new ArrayList<>();
        List<Integer> femaleArr = new ArrayList<>();

        Map<String, int[]> industryNatureData = new HashMap<>();
        jobList.forEach(job -> {
            String key = job.getIndustryNature();

            if (StrUtil.isNotBlank(key)) {
                int[] val = industryNatureData.get(key);
                if (val == null) {
                    val = new int[2];
                    if ("男".equals(job.getSex())) {
                        val[0] = val[0] + 1;
                    } else if ("女".equals(job.getSex())) {
                        val[1] = val[1] + 1;
                    }
                    industryNatureData.put(key, val);
                } else {
                    if ("男".equals(job.getSex())) {
                        val[0] = val[0] + 1;
                    } else if ("女".equals(job.getSex())) {
                        val[1] = val[1] + 1;
                    }
                    industryNatureData.put(key, val);
                }
            }

        });

        for (Map.Entry<String, int[]> stringEntry : industryNatureData.entrySet()) {
            int[] value = stringEntry.getValue();
            maleArr.add(value[0]);
            femaleArr.add(value[1]);
        }

        resultMap.put("industryNatureData", industryNatureData);
        resultMap.put("maleArr", maleArr);
        resultMap.put("femaleArr", femaleArr);

        return ResultVOUtil.success(resultMap);
    }

}
