package com.hqy.YunBI.model.dto.chart;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 编辑请求
 *
 * @author hqy
 * 
 */
@Data
public class ChartEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;


    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表名称
     */
    private String name;
    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;

}