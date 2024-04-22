package com.hqy.YunBI.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ExcelUtils {
    public static String excelToCsv(MultipartFile multipartFile){

        List<Map<Integer,String>> list = null;
        try {

            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();

        } catch (IOException e) {
            log.error("表格处理错误",e);
        }

        if(CollUtil.isEmpty(list)){
            return "";
        }
        //转为CSV
        StringBuilder stringBuilder = new StringBuilder();
        //读取表头第一行  list：<0,日期>,<1,人数>  => 日期，人数
        LinkedHashMap<Integer,String> headerMap = (LinkedHashMap) list.get(0);
        List<String> collect = headerMap.values().stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        stringBuilder.append(StringUtils.join(collect,",")).append("\n");


        for(int i=1;i<list.size();i++){
            LinkedHashMap<Integer,String> dataMap = (LinkedHashMap) list.get(i);
            List<String> dataCollect  = dataMap.values().stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(dataCollect,",")).append("\n");
        }



        return stringBuilder.toString();
    }

}
