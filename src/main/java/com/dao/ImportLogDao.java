package com.dao;

import com.model.ImportLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 导入记录DAO
 *
 * @author Clrvn
 */
public interface ImportLogDao {

    /**
     * 通过id查询单个导入记录
     */
    ImportLog findById(Integer id);

    /**
     * 通过map查询单个导入记录
     */
    ImportLog findByMap(Map<String, Object> map);

    /**
     * 查询导入记录列表
     */
    List<ImportLog> list(Map<String, Object> map);

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
     * 获取导入时间
     */
    List<String> getImportTime();

    void bitchSave(@Param("importLogList") List<ImportLog> importLogList);
}
