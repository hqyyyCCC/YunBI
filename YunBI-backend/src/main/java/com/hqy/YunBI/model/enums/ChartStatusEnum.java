package com.hqy.YunBI.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ChartStatusEnum {
    WATTING("排队中","watting"),
    RUNNING("智能分析中","running"),
    SUCCEED("已完成","succeed"),
    FAILED("分析失败","failed");

    private final String text;
    private final String value;

    ChartStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }
    public static ChartStatusEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ChartStatusEnum anEnum : ChartStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
