package com.example.demo.gift.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 礼单导入行（Excel/CSV 统一列定义）
 * 金额、日期、方向均以字符串读取，避免精度/格式问题，解析逻辑在 Service 中处理
 */
@Data
public class GiftImportRow {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("金额")
    private String amount;

    @ExcelProperty("方向")
    private String direction;

    @ExcelProperty("日期")
    private String giftDate;

    @ExcelProperty("事由")
    private String occasion;

    @ExcelProperty("关系")
    private String relation;

    @ExcelProperty("备注")
    private String remark;
}
