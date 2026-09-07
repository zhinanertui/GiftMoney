package com.example.demo.gift.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.gift.dto.ContactStat;
import com.example.demo.gift.dto.DirectionStat;
import com.example.demo.gift.dto.GiftImportRow;
import com.example.demo.gift.dto.ImportResult;
import com.example.demo.gift.dto.MonthlyStat;
import com.example.demo.gift.dto.OccasionStat;
import com.example.demo.gift.entity.GiftRecord;
import com.example.demo.gift.mapper.GiftRecordMapper;
import com.example.demo.gift.service.GiftRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class GiftRecordServiceImpl extends ServiceImpl<GiftRecordMapper, GiftRecord> implements GiftRecordService {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy年M月d日"),
            DateTimeFormatter.ofPattern("yyyyMMdd")
    );

    @Override
    public IPage<GiftRecord> page(Long userId, long current, long size, String keyword, Integer direction, Integer year, Integer repayStatus) {
        LambdaQueryWrapper<GiftRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GiftRecord::getUserId, userId);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(GiftRecord::getName, keyword);
        }
        if (direction != null) {
            wrapper.eq(GiftRecord::getDirection, direction);
        }
        if (year != null) {
            wrapper.ge(GiftRecord::getGiftDate, LocalDate.of(year, 1, 1))
                    .lt(GiftRecord::getGiftDate, LocalDate.of(year + 1, 1, 1));
        }
        // 归还状态筛选：先按 姓名+事由 汇总收支，筛出符合条件的组合
        if (repayStatus != null) {
            applyRepayStatusFilter(wrapper, userId, repayStatus);
        }
        wrapper.orderByDesc(GiftRecord::getGiftDate).orderByDesc(GiftRecord::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    /**
     * 按归还状态筛选送出记录
     * 1未归还 2部分归还 3已还清
     */
    private void applyRepayStatusFilter(LambdaQueryWrapper<GiftRecord> wrapper, Long userId, Integer repayStatus) {
        List<ContactStat> stats = baseMapper.contactsStat(userId);
        List<ContactStat> matched = stats.stream()
                .filter(c -> {
                    BigDecimal sent = c.getSent() == null ? BigDecimal.ZERO : c.getSent();
                    BigDecimal received = c.getReceived() == null ? BigDecimal.ZERO : c.getReceived();
                    if (sent.signum() <= 0) {
                        return false; // 仅收到的组合不参与
                    }
                    if (repayStatus == 1) {
                        return received.signum() == 0;               // 未归还
                    } else if (repayStatus == 2) {
                        return received.signum() > 0 && received.compareTo(sent) < 0; // 部分归还
                    } else {
                        return received.compareTo(sent) >= 0;         // 已还清
                    }
                })
                .collect(java.util.stream.Collectors.toList());

        if (matched.isEmpty()) {
            // 无匹配：加一个恒假条件，返回空分页
            wrapper.apply("1 = 0");
            return;
        }

        wrapper.eq(GiftRecord::getDirection, 1);
        wrapper.and(w -> {
            for (int i = 0; i < matched.size(); i++) {
                ContactStat c = matched.get(i);
                if (i > 0) {
                    w.or();
                }
                w.eq(GiftRecord::getName, c.getName())
                 .eq(GiftRecord::getOccasion, c.getOccasion());
            }
        });
    }

    @Override
    public ImportResult importFile(Long userId, MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        List<GiftImportRow> rows;
        try {
            if (filename.endsWith(".csv")) {
                rows = parseCsv(file);
            } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                rows = parseExcel(file);
            } else {
                return ImportResult.of(0, 0, "不支持的文件类型，请上传 .xlsx 或 .csv");
            }
        } catch (Exception e) {
            log.error("导入解析失败", e);
            return ImportResult.of(0, 0, "文件解析失败: " + e.getMessage());
        }

        if (rows.isEmpty()) {
            return ImportResult.of(0, 0, "文件中没有可导入的数据");
        }

        int success = 0;
        int failed = 0;
        List<GiftRecord> toSave = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            GiftImportRow row = rows.get(i);
            int lineNo = i + 2; // 表头占第 1 行
            try {
                GiftRecord record = convert(userId, row);
                toSave.add(record);
                success++;
            } catch (Exception e) {
                failed++;
                if (errors.size() < 5) {
                    errors.add("第" + lineNo + "行: " + e.getMessage());
                }
            }
        }

        if (!toSave.isEmpty()) {
            this.saveBatch(toSave);
        }

        String msg = "成功导入 " + success + " 条，失败 " + failed + " 条";
        if (!errors.isEmpty()) {
            msg += "。失败原因示例：" + String.join("；", errors);
        }
        return ImportResult.of(success, failed, msg);
    }

    @Override
    public List<MonthlyStat> monthlyStat(Long userId) {
        return baseMapper.monthlyStat(userId);
    }

    @Override
    public List<DirectionStat> directionStat(Long userId) {
        return baseMapper.directionStat(userId);
    }

    @Override
    public List<OccasionStat> occasionStat(Long userId) {
        return baseMapper.occasionStat(userId);
    }

    @Override
    public List<ContactStat> contactsStat(Long userId) {
        return baseMapper.contactsStat(userId);
    }

    @Override
    public IPage<ContactStat> contactsStatPage(Long userId, long current, long size, String keyword) {
        return baseMapper.contactsStatPage(new Page<>(current, size), userId, keyword);
    }

    // ---------- 导入解析 ----------

    private List<GiftImportRow> parseExcel(MultipartFile file) throws IOException {
        List<GiftImportRow> rows = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), GiftImportRow.class, new GiftImportListener(rows)).sheet().doRead();
        return rows;
    }

    private List<GiftImportRow> parseCsv(MultipartFile file) throws IOException {
        List<GiftImportRow> rows = new ArrayList<>();
        // 使用 UTF-8 BOM 感知读取，兼容 Excel 导出的 CSV
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.mark(1);
            int bom = reader.read();
            if (bom != 0xFEFF) {
                reader.reset();
            }
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
            try (CSVParser parser = format.parse(reader)) {
                for (CSVRecord record : parser) {
                    GiftImportRow row = new GiftImportRow();
                    row.setName(get(record, "姓名"));
                    row.setAmount(get(record, "金额"));
                    row.setDirection(get(record, "方向"));
                    row.setGiftDate(get(record, "日期"));
                    row.setOccasion(get(record, "事由"));
                    row.setRelation(get(record, "关系"));
                    row.setRemark(get(record, "备注"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private String get(CSVRecord record, String header) {
        try {
            return record.isMapped(header) ? record.get(header) : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- 行转换 ----------

    private GiftRecord convert(Long userId, GiftImportRow row) {
        String name = trim(row.getName());
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("姓名不能为空");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(trim(row.getAmount()).replace(",", "").replace("，", ""));
        } catch (Exception e) {
            throw new IllegalArgumentException("金额格式错误: " + row.getAmount());
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("金额不能为负数");
        }

        Integer direction = parseDirection(row.getDirection());
        LocalDate date = parseDate(row.getGiftDate());

        String occasion = trim(row.getOccasion());
        if (!StringUtils.hasText(occasion)) {
            throw new IllegalArgumentException("事由不能为空");
        }

        GiftRecord record = new GiftRecord();
        record.setUserId(userId);
        record.setName(name);
        record.setAmount(amount);
        record.setDirection(direction);
        record.setGiftDate(date);
        record.setOccasion(occasion);
        record.setRelation(trim(row.getRelation()));
        record.setRemark(trim(row.getRemark()));
        return record;
    }

    private Integer parseDirection(String raw) {
        String s = trim(raw);
        if (!StringUtils.hasText(s)) {
            throw new IllegalArgumentException("方向不能为空（填：送出 / 收到）");
        }
        if ("1".equals(s) || s.contains("送") || s.contains("出") || s.contains("给")) {
            return 1;
        }
        if ("2".equals(s) || s.contains("收") || s.contains("还") || s.contains("入") || s.contains("来")) {
            return 2;
        }
        throw new IllegalArgumentException("方向无法识别: " + raw + "（应填 送出 或 收到）");
    }

    private LocalDate parseDate(String raw) {
        String s = trim(raw);
        if (!StringUtils.hasText(s)) {
            throw new IllegalArgumentException("日期不能为空");
        }
        // 兼容 Excel 数字日期（1900 日期系统）
        if (s.matches("\\d{5}")) {
            long serial = Long.parseLong(s);
            if (serial >= 20000 && serial <= 60000) {
                return LocalDate.of(1899, 12, 30).plusDays(serial);
            }
        }
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(s, fmt);
            } catch (Exception ignored) {
                // 尝试下一种格式
            }
        }
        throw new IllegalArgumentException("日期格式无法识别: " + raw + "（应为 yyyy-MM-dd）");
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
