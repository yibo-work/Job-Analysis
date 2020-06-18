package com.service;

import com.github.pagehelper.PageInfo;
import com.model.ImportLog;
import com.utils.RequestParamsUtil;
import com.vo.ResultVO;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 导入记录Service
 *
 * @author Clrvn
 */
public interface ImportLogService {

    /**
     * 查询导入记录页面
     *
     * @return 分页导入记录数据
     */
    PageInfo<ImportLog> page(RequestParamsUtil requestParamsUtil);

    /**
     * 查询导入记录列表
     */
    List<ImportLog> list(Map<String, Object> map);

    /**
     * 通过id查询单个导入记录
     */
    ImportLog findById(Integer id);

    /**
     * 通过map查询单个导入记录
     */
    ImportLog findByMap(Map<String, Object> map);

    /**
     * 新增导入记录
     */
    int save(ImportLog importLog);

    /**
     * 修改导入记录
     */
    int update(ImportLog importLog);

    /**
     * 删除导入记录
     */
    int deleteById(Integer id);

    /**
     * 导入数据
     */
    ResultVO importData(File targetFile) throws Exception;

    /**
     * 获取导入时间下拉框数据
     */
    List<String> getImportTime();

    /**
     * 分析数据
     */
    ResultVO getAnalysisData(Map<String, Object> parameters);

    /**
     * 分析学生
     */
    ResultVO analysisCanteen(Map<String, Object> parameters);

}
