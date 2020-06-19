package com.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.dao.ImportLogDao;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.model.ImportLog;
import com.service.ImportLogService;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 导入记录ServiceImpl
 *
 * @author Clrvn
 */
@Service
public class ImportLogServiceImpl implements ImportLogService {

    @Autowired
    private ImportLogDao importLogDao;

    /**
     * 查询导入记录页面
     *
     * @return 分页导入记录数据
     */
    @Override
    public PageInfo<ImportLog> page(RequestParamsUtil requestParamsUtil) {
        PageHelper.startPage(requestParamsUtil.getPageNo(), requestParamsUtil.getPageSize());
        return new PageInfo<>(importLogDao.list(requestParamsUtil.getParameters()));
    }

    /**
     * 查询导入记录列表
     */
    @Override
    public List<ImportLog> list(Map<String, Object> map) {
        return importLogDao.list(map);
    }


    /**
     * 通过id查询单个导入记录
     */
    @Override
    public ImportLog findById(Integer id) {
        return importLogDao.findById(id);
    }

    /**
     * 通过map查询单个导入记录
     */
    @Override
    public ImportLog findByMap(Map<String, Object> map) {
        return importLogDao.findByMap(map);
    }

    /**
     * 新增导入记录
     */
    @Override
    public int save(ImportLog importLog) {
        return importLogDao.save(importLog);
    }

    /**
     * 修改导入记录
     */
    @Override
    public int update(ImportLog importLog) {
        return importLogDao.update(importLog);
    }

    /**
     * 删除导入记录
     */
    @Override
    public int deleteById(Integer id) {
        return importLogDao.deleteById(id);
    }


    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public ResultVO importData(File targetFile) throws Exception {

        ResultVO resultVO = new ResultVO();

        String originalFilename = targetFile.getName();
        // 解析excel的第一个sheet页，导入全部资质的数据
        int columnsForRead = 6;
        List<List<String>> list;
        if (originalFilename.toLowerCase().endsWith(".xlsx")) {
            list = XLSXCovertCSVUtil.readXLSX(targetFile, 1, 2, columnsForRead);
        } else {
            list = XLSConvertCSVUtil.readXLS(targetFile, 1, 2, columnsForRead);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm");
        List<ImportLog> importLogList = new ArrayList<>(list.size());
        for (List<String> strings : list) {
            ImportLog importLog = new ImportLog();
            importLog.setStudentNo(strings.get(0));
            importLog.setSex(strings.get(1));
            String schoolCode = strings.get(2);
            schoolCode = schoolCode.substring(0, schoolCode.indexOf("-"));

            //只取学院号
            importLog.setSchoolCode(schoolCode);
            importLog.setMoney(new BigDecimal(strings.get(3)));
            importLog.setTime(sdf.parse(strings.get(4)));
            importLog.setStation(strings.get(5));
            importLogList.add(importLog);
        }

        if (importLogList.isEmpty()) {
            resultVO.setCode(500);
            resultVO.setMsg("导入失败,无数据！");
        } else {
            importLogDao.bitchSave(importLogList);
            resultVO.setCode(200);
            resultVO.setMsg("导入成功！");
        }
        return resultVO;
    }

    @Override
    public List<String> getImportTime() {
        return importLogDao.getImportTime();
    }

    /**
     * 分析数据
     */
    @Override
    public ResultVO getAnalysisData(Map<String, Object> parameters) {

        Map<String, Object> resultMap = new HashMap<>(8);

        //获取综合分析数据
        HashMap<String, Object> commonMap = new HashMap<>(4);
        commonMap.put("createTime", parameters.get("createTime"));
        List<ImportLog> commonData = importLogDao.list(commonMap);

        if (!commonData.isEmpty()) {
            //综合分析
            Map<String, Object> commonAnalysis = analysisDataByImportList(commonData);
            resultMap.put("commonAnalysis", commonAnalysis);

            //获取学生个人分析数据
            HashMap<String, Object> studentNoMap = new HashMap<>(4);
            studentNoMap.put("studentNo", parameters.get("studentNo"));
            studentNoMap.put("createTime", parameters.get("createTime"));
            studentNoMap.put("sex", parameters.get("sex"));
            studentNoMap.put("schoolCode", parameters.get("schoolCode"));
            studentNoMap.put("grade", parameters.get("grade"));

            if (ObjectUtil.isNotEmpty(parameters.get("studentNo"))) {
                List<ImportLog> studentNoData = importLogDao.list(studentNoMap);
                resultMap.put("studentNoData", studentNoData);
                if (!studentNoData.isEmpty()) {
                    //学生个人分析
                    Map<String, Object> studentNoAnalysis = analysisDataByImportList(studentNoData);
                    Map<String, Object> studentMap = analysisStudentData(studentNoData);
                    studentNoAnalysis.put("situationList", studentMap.get("situationList"));
                    resultMap.put("studentNoAnalysis", studentNoAnalysis);

                }
            }

            //获取性别分析数据
            HashMap<String, Object> sexMap = new HashMap<>(4);
            sexMap.put("sex", parameters.get("sex"));
            sexMap.put("createTime", parameters.get("createTime"));

            if (ObjectUtil.isNotEmpty(parameters.get("sex"))) {
                List<ImportLog> sexData = importLogDao.list(sexMap);
                if (!sexData.isEmpty()) {
                    //性别分析
                    Map<String, Object> sexAnalysis = analysisDataByImportList(sexData);
                    resultMap.put("sexAnalysis", sexAnalysis);

                }
            }

            //获取学校分析数据
            HashMap<String, Object> schoolCodeMap = new HashMap<>(4);
            schoolCodeMap.put("schoolCode", parameters.get("schoolCode"));
            schoolCodeMap.put("createTime", parameters.get("createTime"));
            if (ObjectUtil.isNotEmpty(parameters.get("schoolCode"))) {
                List<ImportLog> schoolCodeData = importLogDao.list(schoolCodeMap);
                if (!schoolCodeData.isEmpty()) {
                    //学校分析
                    Map<String, Object> schoolCodeAnalysis = analysisDataByImportList(schoolCodeData);
                    resultMap.put("schoolCodeAnalysis", schoolCodeAnalysis);

                }
            }

            //获取年级分析数据
            HashMap<String, Object> gradeMap = new HashMap<>(4);
            gradeMap.put("grade", parameters.get("grade"));
            gradeMap.put("createTime", parameters.get("createTime"));

            if (ObjectUtil.isNotEmpty(parameters.get("grade"))) {
                List<ImportLog> gradeData = importLogDao.list(gradeMap);
                if (!gradeData.isEmpty()) {
                    //年级分析
                    Map<String, Object> gradeAnalysis = analysisDataByImportList(gradeData);
                    resultMap.put("gradeAnalysis", gradeAnalysis);

                }
            }

        }

        return ResultVOUtil.success(resultMap);
    }

    /**
     * 分析数据
     *
     * @param importLogList 导入列表
     * @return 分析后的数据
     */
    private Map<String, Object> analysisDataByImportList(List<ImportLog> importLogList) {

        Map<String, Object> resultMap = new HashMap<>();

        if (CollectionUtil.isEmpty(importLogList)) {
            return resultMap;
        }

        int nums = importLogList.size();
        //就业总额
        double sumMoney = importLogList.stream().mapToDouble(importLog -> importLog.getMoney().doubleValue()).sum();

        Date startDate = importLogList.get(0).getTime();
        Date endDate = importLogList.get(nums - 1).getTime();

        //相差天数
        long betweenDay = DateUtil.between(startDate, endDate, DateUnit.DAY);

        //计算时间包含多少自然月
        Set<Integer> monthSet = new HashSet<>();
        importLogList.forEach(importLog -> {
            int month = DateUtil.month(importLog.getTime());
            monthSet.add(month);
        });

        int monthNum = monthSet.size();

        //日均就业额
        String dayUse = String.format("%.2f", sumMoney / betweenDay);
        resultMap.put("dayUse", dayUse);

        // 月均就业额
        String monthUse = String.format("%.2f", sumMoney / monthNum);
        resultMap.put("monthUse", monthUse);

        // 每日就业频率
        String dayFrequency = String.format("%.2f", (double) nums / betweenDay);

        //每月就业频率
        String monthFrequency = String.format("%.2f", (double) nums / monthNum);

        resultMap.put("dayFrequency", dayFrequency);
        resultMap.put("monthFrequency", monthFrequency);

        //就业地点偏好
        Map<String, Integer> stationMap = new HashMap<>(nums);
        importLogList.forEach(importLog -> {

            String station = importLog.getStation();
            if (stationMap.get(station) == null) {
                stationMap.put(station, 0);
            } else {
                stationMap.put(station, stationMap.get(station) + 1);
            }

        });

        //最常去的三家学生编号倒序
        List<Map.Entry<String, Integer>> list = new ArrayList<>(stationMap.entrySet());
        list.sort((o1, o2) -> {
            int compare = (o1.getValue()).compareTo(o2.getValue());
            return -compare;
        });

        int i = 0;
        for (Map.Entry<String, Integer> entry : list) {

            if (i == 0) {
                String firstStation = entry.getKey();
                resultMap.put("firstStation", firstStation);
            } else if (i == 1) {
                String secondStation = entry.getKey();
                resultMap.put("secondStation", secondStation);
            } else if (i == 2) {
                String thirdStation = entry.getKey();
                resultMap.put("thirdStation", thirdStation);
            }

            i++;
        }

        resultMap.put("stationPreference", stationMap);

        //就业时间偏好
        Map<String, Integer> timeMap = new HashMap<>(nums);
        importLogList.forEach(importLog -> {

            int hour = DateUtil.hour(importLog.getTime(), true);

            if (hour >= 6 && hour < 8) {
                String key = "6~8";
                Integer value = timeMap.get(key);
                if (value == null) {
                    timeMap.put(key, 0);
                } else {
                    timeMap.put(key, value + 1);
                }
            } else if (hour >= 8 && hour < 10) {
                String key = "8~10";
                Integer value = timeMap.get(key);
                if (value == null) {
                    timeMap.put(key, 0);
                } else {
                    timeMap.put(key, value + 1);
                }
            } else if (hour >= 10 && hour < 12) {
                String key = "10~12";
                Integer value = timeMap.get(key);
                if (value == null) {
                    timeMap.put(key, 0);
                } else {
                    timeMap.put(key, value + 1);
                }
            } else if (hour >= 12 && hour < 14) {
                String key = "12~14";
                Integer value = timeMap.get(key);
                if (value == null) {
                    timeMap.put(key, 0);
                } else {
                    timeMap.put(key, value + 1);
                }
            } else if (hour >= 14 && hour < 16) {
                String key = "14~16";
                Integer value = timeMap.get(key);
                if (value == null) {
                    timeMap.put(key, 0);
                } else {
                    timeMap.put(key, value + 1);
                }
            } else if (hour >= 16 && hour < 18) {
                String key = "16~18";
                Integer value = timeMap.get(key);
                if (value == null) {
                    timeMap.put(key, 0);
                } else {
                    timeMap.put(key, value + 1);
                }
            } else if (hour >= 18 && hour < 20) {
                String key = "18~20";
                Integer value = timeMap.get(key);
                if (value == null) {
                    timeMap.put(key, 0);
                } else {
                    timeMap.put(key, value + 1);
                }
            } else if (hour >= 20 && hour < 22) {
                String key = "20~22";
                Integer value = timeMap.get(key);
                if (value == null) {
                    timeMap.put(key, 0);
                } else {
                    timeMap.put(key, value + 1);
                }
            }

        });

        resultMap.put("timePreference", timeMap);

        return resultMap;
    }

    /**
     * 分析学生个人就业情况
     */
    private Map<String, Object> analysisStudentData(List<ImportLog> studentList) {
        Map<String, Object> resultMap = new HashMap<>();
        //学生就业的最后一天
        Date endTime = studentList.get(studentList.size() - 1).getTime();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("endTime", DateUtil.format(endTime, "yyyy-MM-dd HH:mm:ss"));
        List<ImportLog> situationList = importLogDao.list(paramMap);

        resultMap.put("situationList", situationList);

        return resultMap;
    }


    /**
     * 学生就业分析
     */
    @Override
    public ResultVO analysisCanteen(Map<String, Object> parameters) {

        ResultVO resultVO = new ResultVO();

        if (ObjectUtil.isNotEmpty(parameters.get("station"))) {

            List<ImportLog> canteenData = importLogDao.list(parameters);

            if (canteenData.isEmpty()) {
                return ResultVOUtil.failure("分析失败，没有找到该学生的就业数据！");
            }

            String station = String.valueOf(parameters.get("station"));

            List<ImportLog> allData = importLogDao.list(new HashMap<>());

            // 所有的店铺及人次
            Map<String, Integer> allStationRank = new HashMap<>(allData.size());

            allData.forEach(importLog -> {
                String station1 = importLog.getStation();
                Integer value = allStationRank.get(station1);
                if (allStationRank.get(station1) == null) {
                    allStationRank.put(station1, 0);
                } else {
                    allStationRank.put(station1, value + 1);
                }
            });

            //倒序排序
            List<Map.Entry<String, Integer>> list = new ArrayList<>(allStationRank.entrySet());
            list.sort((o1, o2) -> {
                int compare = (o1.getValue()).compareTo(o2.getValue());
                return -compare;
            });

            Map<String, Object> stationMap = new HashMap<>();
            int i = 1;
            for (Map.Entry<String, Integer> entry : list) {

                if (entry.getKey().equals(station)) {
                    //就业人次在总店铺的排名
                    stationMap.put("rankNo", i);
                }
                i++;
            }


            int nums = canteenData.size();

            stationMap.put("nums", nums);
            //就业总额
            double sumMoney = canteenData.stream().mapToDouble(importLog -> importLog.getMoney().doubleValue()).sum();

            //计算时间包含多少自然月
            Set<Integer> monthSet = new HashSet<>();
            canteenData.forEach(importLog -> {
                int month = DateUtil.month(importLog.getTime());
                monthSet.add(month);
            });

            int monthNum = monthSet.size();

            // 月均就业额
            String monthUse = String.format("%.2f", sumMoney / monthNum);
            stationMap.put("monthUse", monthUse);

            //人均就业金额
            String oneUser = String.format("%.2f", sumMoney / nums);
            stationMap.put("oneUser", oneUser);

            resultVO.setCode(200);
            resultVO.setMsg("分析成功！");
            resultVO.setData(stationMap);

        } else {
            return ResultVOUtil.failure("分析失败，数据为空,请导入数据后再分析！");
        }

        return resultVO;
    }

}
