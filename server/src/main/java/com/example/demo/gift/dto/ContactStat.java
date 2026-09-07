package com.example.demo.gift.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 按人往来汇总：同一姓名的送出/收到（还礼）金额
 */
@Data
public class ContactStat {

    /** 对方姓名 */
    private String name;

    /** 事由（结婚/满月/生日等） */
    private String occasion;

    /** 我送出的总额 */
    private BigDecimal sent;

    /** 对方还礼的总额 */
    private BigDecimal received;

    /** 差额 received - sent（正=对方还多了，负=还欠我） */
    private BigDecimal balance;
}
