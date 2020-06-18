package com.controller;

import com.enums.ResultFailureEnum;
import com.github.pagehelper.PageInfo;
import com.model.ImportLog;
import com.service.ImportLogService;
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
 * 导入记录Controller
 *
 * @author Clrvn
 */
@RestController
@RequestMapping("/importLog")
public class ImportLogController {

    @Autowired
    private ImportLogService importLogService;

    /**
     * 查询导入记录页面
     *
     * @return 分页导入记录数据
     */
    @GetMapping("/page")
    public PageInfo<ImportLog> page() {
        return importLogService.page(new RequestParamsUtil());
    }

    /**
     * 查询导入记录列表
     */
    @GetMapping("/list")
    public ResultVO list() {
        List<ImportLog> importLogList = importLogService.list(new RequestParamsUtil().getParameters());
        return ResultVOUtil.success(importLogList);
    }

    /**
     * 通过id查询单个导入记录
     */
    @GetMapping("/findById")
    public ResultVO findById(@RequestParam(value = "id") Integer id) {
        ImportLog importLog = importLogService.findById(id);
        return ResultVOUtil.success(importLog);
    }

    /**
     * 通过map查询单个导入记录
     */
    @GetMapping("/findByMap")
    public ResultVO findByMap() {
        ImportLog importLog = importLogService.findByMap(new RequestParamsUtil().getParameters());
        return ResultVOUtil.success(importLog);
    }

    /**
     * 添加导入记录
     */
    @PostMapping("/save")
    public ResultVO save(@RequestBody ImportLog importLog) {
        importLogService.save(importLog);
        return ResultVOUtil.success();
    }

    /**
     * 修改导入记录
     */
    @PutMapping("/update")
    public ResultVO update(@RequestBody ImportLog importLog) {
        importLogService.update(importLog);
        return ResultVOUtil.success();
    }

    /**
     * 删除导入记录
     */
    @DeleteMapping("/deleteById")
    public ResultVO deleteById(@RequestParam(value = "id") Integer id) {
        importLogService.deleteById(id);
        return ResultVOUtil.success();

    }

    /**
     * 导入数据
     */
    @PostMapping("/importData")
    public ResultVO importData(MultipartFile file) {
        try {
            //文件上传
            FileUtil.uploadFile(file.getBytes(), FileUtil.UPLOAD_PATH + file.getOriginalFilename());
            return importLogService.importData(new File(FileUtil.UPLOAD_PATH + file.getOriginalFilename()));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
            return ResultVOUtil.failure(ResultFailureEnum.IMPORT_ERROR);
        }

    }

    /**
     * 查询导入时间
     */
    @GetMapping("/getImportTime")
    public ResultVO getImportTime() {
        List<String> importTimeList = importLogService.getImportTime();
        return ResultVOUtil.success(importTimeList);
    }


    /**
     * 分析数据然后返回
     */
    @GetMapping("/getAnalysisData")
    public ResultVO getAnalysisData() {
        try {
            return importLogService.getAnalysisData(new RequestParamsUtil().getParameters());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
            return ResultVOUtil.failure("数据分析异常！");
        }

    }

    /**
     * 分析学生消费数据
     */
    @GetMapping("/getAnalysisCanteen")
    public ResultVO analysisCanteen() {
        try {
            return importLogService.analysisCanteen(new RequestParamsUtil().getParameters());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
            return ResultVOUtil.failure("数据分析异常！");
        }

    }

}