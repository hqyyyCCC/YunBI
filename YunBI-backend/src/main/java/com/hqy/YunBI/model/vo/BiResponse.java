package com.hqy.YunBI.model.vo;

import lombok.Data;

@Data
public class BiResponse {
    private String genChart;
    private String genResult;
    //新生成图表ID
    private Long chartId;
}
