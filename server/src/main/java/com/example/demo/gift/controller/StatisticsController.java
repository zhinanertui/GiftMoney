package com.example.demo.gift.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.demo.common.Result;
import com.example.demo.gift.dto.DirectionStat;
import com.example.demo.gift.dto.MonthlyStat;
import com.example.demo.gift.dto.OccasionStat;
import com.example.demo.gift.service.GiftRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 统计接口：汇总、饼图、树状图数据
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final GiftRecordService giftRecordService;

    private static String directionName(Integer direction) {
        return direction != null && direction == 2 ? "收到(还礼)" : "送出";
    }

    /**
     * 总览：送出总额、收到总额、差额、总笔数
     * GET /api/stats/overview
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<DirectionStat> stats = giftRecordService.directionStat(userId);

        BigDecimal sent = BigDecimal.ZERO;
        BigDecimal received = BigDecimal.ZERO;
        for (DirectionStat s : stats) {
            if (s.getDirection() != null && s.getDirection() == 2) {
                received = received.add(s.getTotal());
            } else {
                sent = sent.add(s.getTotal());
            }
        }
        long count = giftRecordService.lambdaQuery()
                .eq(com.example.demo.gift.entity.GiftRecord::getUserId, userId)
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("sent", sent);
        data.put("received", received);
        data.put("balance", received.subtract(sent));
        data.put("count", count);
        return Result.ok(data);
    }

    /**
     * 方向汇总（饼图：送出 vs 收到）
     * GET /api/stats/direction
     */
    @GetMapping("/direction")
    public Result<List<Map<String, Object>>> direction() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<DirectionStat> stats = giftRecordService.directionStat(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (DirectionStat s : stats) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", directionName(s.getDirection()));
            item.put("value", s.getTotal());
            result.add(item);
        }
        return Result.ok(result);
    }

    /**
     * 事由汇总（饼图）
     * GET /api/stats/occasion
     */
    @GetMapping("/occasion")
    public Result<List<Map<String, Object>>> occasion() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<OccasionStat> stats = giftRecordService.occasionStat(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (OccasionStat s : stats) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", s.getOccasion() == null ? "未分类" : s.getOccasion());
            item.put("value", s.getTotal());
            result.add(item);
        }
        return Result.ok(result);
    }

    /**
     * 树状图数据：年 → 月 → 方向
     * GET /api/stats/treemap
     */
    @GetMapping("/treemap")
    public Result<List<Map<String, Object>>> treemap() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<MonthlyStat> stats = giftRecordService.monthlyStat(userId);

        // 按年份聚合
        Map<Integer, Map<Integer, Map<Integer, BigDecimal>>> yearMap = new TreeMap<>();
        for (MonthlyStat s : stats) {
            yearMap.computeIfAbsent(s.getYear(), k -> new TreeMap<>())
                    .computeIfAbsent(s.getMonth(), k -> new LinkedHashMap<>())
                    .merge(s.getDirection(), s.getTotal(), BigDecimal::add);
        }

        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, Map<Integer, BigDecimal>>> yearEntry : yearMap.entrySet()) {
            Map<String, Object> yearNode = new HashMap<>();
            yearNode.put("name", yearEntry.getKey() + "年");
            List<Map<String, Object>> monthChildren = new ArrayList<>();

            for (Map.Entry<Integer, Map<Integer, BigDecimal>> monthEntry : yearEntry.getValue().entrySet()) {
                Map<String, Object> monthNode = new HashMap<>();
                monthNode.put("name", monthEntry.getKey() + "月");
                List<Map<String, Object>> dirChildren = new ArrayList<>();

                for (Map.Entry<Integer, BigDecimal> dirEntry : monthEntry.getValue().entrySet()) {
                    Map<String, Object> dirNode = new HashMap<>();
                    dirNode.put("name", directionName(dirEntry.getKey()));
                    dirNode.put("value", dirEntry.getValue());
                    dirChildren.add(dirNode);
                }
                monthNode.put("children", dirChildren);
                monthChildren.add(monthNode);
            }
            yearNode.put("children", monthChildren);
            tree.add(yearNode);
        }
        return Result.ok(tree);
    }

    /**
     * 按月汇总（柱状图数据）：返回 [{label, sent, received}]，按时间排序
     * GET /api/stats/monthly
     */
    @GetMapping("/monthly")
    public Result<List<Map<String, Object>>> monthly() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<MonthlyStat> stats = giftRecordService.monthlyStat(userId);

        Map<String, BigDecimal> sentMap = new LinkedHashMap<>();
        Map<String, BigDecimal> receivedMap = new LinkedHashMap<>();
        for (MonthlyStat s : stats) {
            String key = String.format("%04d-%02d", s.getYear(), s.getMonth());
            if (s.getDirection() != null && s.getDirection() == 2) {
                receivedMap.merge(key, s.getTotal(), BigDecimal::add);
            } else {
                sentMap.merge(key, s.getTotal(), BigDecimal::add);
            }
        }

        TreeSet<String> keys = new TreeSet<>();
        keys.addAll(sentMap.keySet());
        keys.addAll(receivedMap.keySet());

        List<Map<String, Object>> result = new ArrayList<>();
        for (String key : keys) {
            Map<String, Object> item = new HashMap<>();
            item.put("label", key);
            item.put("sent", sentMap.getOrDefault(key, BigDecimal.ZERO));
            item.put("received", receivedMap.getOrDefault(key, BigDecimal.ZERO));
            result.add(item);
        }
        return Result.ok(result);
    }
}
