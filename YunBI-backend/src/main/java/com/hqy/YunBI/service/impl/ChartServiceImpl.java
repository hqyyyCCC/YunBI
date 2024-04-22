package com.hqy.YunBI.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.hqy.YunBI.constant.CommonConstant;
import com.hqy.YunBI.mapper.ChartMapper;
import com.hqy.YunBI.model.dto.chart.ChartQueryRequest;
import com.hqy.YunBI.model.entity.Chart;
import com.hqy.YunBI.service.ChartService;
import com.hqy.YunBI.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author 原生优质青年
* @description 针对表【chart(图表信息)】的数据库操作Service实现
* @createDate 2024-04-05 09:56:22
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {

        QueryWrapper<Chart> chartQueryWrapper = new QueryWrapper<>();
        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String chartData = chartQueryRequest.getChartData();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        int current = chartQueryRequest.getCurrent();
        int pageSize = chartQueryRequest.getPageSize();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        chartQueryWrapper.eq(id != null && id>0,"id",id);
        chartQueryWrapper.eq(StringUtils.isNotBlank(goal),"goal",goal);
        chartQueryWrapper.eq(StringUtils.isNotBlank(chartData),"chartData",goal);
        chartQueryWrapper.eq(StringUtils.isNotBlank(chartType),"chartType",goal);
        chartQueryWrapper.eq("isDelete",false);

        chartQueryWrapper.orderBy(SqlUtils.validSortField(sortField),sortOrder.equals(CommonConstant.SORT_ORDER_ASC),sortField);
        return chartQueryWrapper;
    }
    /**
     * 处理图表更新错误的情况。
     *
     * @param chartId 图表的ID，用于标识需要更新的图表。
     * @param execMessage 更新执行过程中遇到的错误信息。
     * 该方法不返回任何值，但会尝试将指定图表的状态更新为"failed"，并记录更新失败的日志。
     */
    @Override
    public void handleChartUpdateError(long chartId, String execMessage){
        // 创建一个新的Chart实例用于更新
        Chart updateChar = new Chart();
        updateChar.setId(chartId); // 设置图表ID
        updateChar.setStatus("failed"); // 设置图表状态为失败
        updateChar.setExecMessage(execMessage); // 设置执行错误信息

        boolean updateResult = updateById(updateChar); // 通过ID更新图表信息
        if(!updateResult){
            // 如果更新失败，记录日志
            log.error("更新图表状态失败" + chartId + "," + execMessage);
        }
    }
}




