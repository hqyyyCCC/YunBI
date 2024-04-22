package com.hqy.YunBI.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hqy.YunBI.model.dto.chart.ChartQueryRequest;

import com.hqy.YunBI.model.entity.Chart;



/**
* @author 原生优质青年
* @description 针对表【chart(图表信息)】的数据库操作Service
* @createDate 2024-04-05 09:56:22
*/
public interface ChartService extends IService<Chart> {
    /**
     * 获取查询条件
     *
     * @param chartQueryRequest
     * @return
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);
    void handleChartUpdateError(long chartId,String execMessage);


}
