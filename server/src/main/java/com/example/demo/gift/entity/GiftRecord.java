package com.example.demo.gift.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 礼单记录表 tb_gift_record
 */
@Data
@TableName("tb_gift_record")
public class GiftRecord {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 归属用户ID */
    private Long userId;

    /** 对方姓名 */
    private String name;

    /** 金额 */
    private BigDecimal amount;

    /** 方向 1送出 2收到(还礼) */
    private Integer direction;

    /** 礼金日期 */
    private LocalDate giftDate;

    /** 事由 */
    private String occasion;

    /** 关系 */
    private String relation;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除 0否 1是 */
    @TableLogic
    private Integer delYn;
}
