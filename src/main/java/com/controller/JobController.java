package com.controller;

import com.enums.ResultFailureEnum;
import com.github.pagehelper.PageInfo;
import com.model.Job;
import com.service.JobService;
import com.utils.FileUtil;
import com.utils.RequestParamsUtil;
import com.utils.ResultVOUtil;
import com.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * 就业Controller
 *
 * @author Clrvn
 */
@RestController
@RequestMapping("/job")
public class JobController {

    @Autowired
    private JobService jobService;

    /**
     * 查询就业页面
     *
     * @return 分页就业数据
     */
    @GetMapping("/page")
    public PageInfo<Job> page() {
        return jobService.page(new RequestParamsUtil());
    }

    /**
     * 查询就业列表
     */
    @GetMapping("/list")
    public ResultVO list() {
        List<Job> jobList = jobService.list(new RequestParamsUtil().getParameters());
        return ResultVOUtil.success(jobList);
    }

    /**
     * 通过id查询单个就业
     */
    @GetMapping("/findById")
    public ResultVO findById(@RequestParam(value = "id") Integer id) {
        Job job = jobService.findById(id);
        return ResultVOUtil.success(job);
    }

    /**
     * 通过map查询单个就业
     */
    @GetMapping("/findByMap")
    public ResultVO findByMap() {
        Job job = jobService.findByMap(new RequestParamsUtil().getParameters());
        return ResultVOUtil.success(job);
    }

    /**
     * 添加就业
     */
    @PostMapping("/save")
    public ResultVO save(@RequestBody Job job) {
        try {
            jobService.save(job);
            return ResultVOUtil.success();
        } catch (Exception ex) {
            return ResultVOUtil.failure("添加失败！");
        }
    }

    /**
     * 修改就业
     */
    @PutMapping("/update")
    public ResultVO update(@RequestBody Job job) {
        try {
            jobService.update(job);
            return ResultVOUtil.success();
        } catch (Exception ex) {
            return ResultVOUtil.failure("修改失败！");
        }

    }

    /**
     * 删除就业
     */
    @DeleteMapping("/deleteById")
    public ResultVO deleteById(@RequestParam(value = "id") Integer id) {
        try {
            jobService.deleteById(id);
            return ResultVOUtil.success();
        } catch (Exception ex) {
            return ResultVOUtil.failure("删除失败！");
        }
    }

    /**
     * 删除就业
     */
    @DeleteMapping("/truncate")
    public ResultVO truncate() {
        try {
            jobService.truncate();
            return ResultVOUtil.success();
        } catch (Exception ex) {
            return ResultVOUtil.failure("删除失败！");
        }
    }

    /**
     * 导入数据
     */
    @PostMapping("/importData")
    public ResultVO importData(MultipartFile file) {
        try {
            //文件上传
            FileUtil.uploadFile(file.getBytes(), FileUtil.UPLOAD_PATH + file.getOriginalFilename());
            return jobService.importData(new File(FileUtil.UPLOAD_PATH + file.getOriginalFilename()));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
            return ResultVOUtil.failure(ResultFailureEnum.IMPORT_ERROR);
        }

    }


    /**
     * 分析数据然后返回
     */
    @GetMapping("/getAnalysisData")
    public ResultVO getAnalysisData() {
        try {
            return jobService.getAnalysisData(new RequestParamsUtil().getParameters());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
            return ResultVOUtil.failure("数据分析异常！");
        }

    }
}