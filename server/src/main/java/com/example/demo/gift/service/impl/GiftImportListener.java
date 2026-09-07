package com.example.demo.gift.service.impl;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.demo.gift.dto.GiftImportRow;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * EasyExcel 读取监听器，逐行收集礼单数据
 */
@Slf4j
public class GiftImportListener extends AnalysisEventListener<GiftImportRow> {

    private final List<GiftImportRow> rows;

    public GiftImportListener(List<GiftImportRow> rows) {
        this.rows = rows;
    }

    @Override
    public void invoke(GiftImportRow data, AnalysisContext context) {
        if (data.getName() != null && !data.getName().trim().isEmpty()) {
            rows.add(data);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // no-op
    }
}
