package com.example.demo.gift.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 统计结果 DTO
 */
@Data
public class DirectionStat {
    private Integer direction;
    private BigDecimal total;
}
