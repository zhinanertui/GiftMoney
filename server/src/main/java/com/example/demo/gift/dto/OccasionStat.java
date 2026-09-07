package com.example.demo.gift.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 按事由的统计结果
 */
@Data
public class OccasionStat {
    private String occasion;
    private BigDecimal total;
}
