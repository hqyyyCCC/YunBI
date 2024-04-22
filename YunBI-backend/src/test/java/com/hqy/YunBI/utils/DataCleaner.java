package com.hqy.YunBI.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DataCleaner {
    @Test
    public void cleanData() {
        List<String> rawData = Arrays.asList(
                "// Echarts V5 option for line chart" +
                        "...\n" +
                        "var option = {\n" +
                        "    xAxis: {\n" +
                        "        type: 'category',\n" +
                        "        data: ['1号', '2号', '3号', '4号', '5号', '6号', '7号', '8号', '9号', '10号']\n" +
                        "    },\n" +
                        "    yAxis: {\n" +
                        "        type: 'value'\n" +
                        "    },\n" +
                        "    series: [{\n" +
                        "        data: [11, 20, 30, 0, 50, 20, 10, 5, 3, 1],\n" +
                        "        type: 'line'\n" +
                        "    }]\n" +
                        "};..."
        );

        // 清洗数据，只保留var option开头以后的内容，并合并为一个String
        String cleanedData = rawData.stream()
                .map(s -> s.substring(s.indexOf("{"),s.lastIndexOf("}")+1))
                .collect(Collectors.joining("\n")); // 使用换行符连接各个字符串片段

        // 打印清洗后的数据或进行其他操作，如存回数据库或返回给前端
        System.out.println(cleanedData);
    }
}
