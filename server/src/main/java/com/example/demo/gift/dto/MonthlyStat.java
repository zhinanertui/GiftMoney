package com.example.demo.gift.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 按年月的统计结果
 */
@Data
public class MonthlyStat {
    private Integer year;
    private Integer month;
    private Integer direction;
    private BigDecimal total;
}
