package com.example.demo.gift.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.gift.dto.ContactStat;
import com.example.demo.gift.dto.ImportResult;
import com.example.demo.gift.entity.GiftRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GiftRecordService extends IService<GiftRecord> {

    /**
     * 分页查询（当前用户，按姓名/事由模糊搜索）
     * repayStatus: 1未归还 2部分归还 3已还清
     */
    IPage<GiftRecord> page(Long userId, long current, long size, String keyword, Integer direction, Integer year, Integer repayStatus);

    /**
     * 导入 Excel/CSV 礼单
     */
    ImportResult importFile(Long userId, MultipartFile file);

    /**
     * 统计：按 年/月/方向 汇总
     */
    List<com.example.demo.gift.dto.MonthlyStat> monthlyStat(Long userId);

    /**
     * 统计：按方向汇总
     */
    List<com.example.demo.gift.dto.DirectionStat> directionStat(Long userId);

    /**
     * 统计：按事由汇总
     */
    List<com.example.demo.gift.dto.OccasionStat> occasionStat(Long userId);

    /**
     * 按人往来汇总（送出 vs 还礼）
     */
    List<ContactStat> contactsStat(Long userId);

    /**
     * 按人往来汇总（分页 + 关键字搜索）
     * repayStatus: 1未归还 2部分归还 3已还清
     */
    IPage<ContactStat> contactsStatPage(Long userId, long current, long size, String keyword);
}
