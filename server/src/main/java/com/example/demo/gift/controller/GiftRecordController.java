package com.example.demo.gift.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.Result;
import com.example.demo.gift.dto.ContactStat;
import com.example.demo.gift.dto.GiftImportRow;
import com.example.demo.gift.dto.ImportResult;
import com.example.demo.gift.entity.GiftRecord;
import com.example.demo.gift.service.GiftRecordService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 礼单管理接口
 */
@RestController
@RequestMapping("/api/gifts")
@RequiredArgsConstructor
public class GiftRecordController {

    private final GiftRecordService giftRecordService;

    /**
     * 分页查询
     * GET /api/gifts?current=&size=&keyword=&direction=&year=&repayStatus=
     */
    @GetMapping
    public Result<IPage<GiftRecord>> page(@RequestParam(defaultValue = "1") long current,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer direction,
                                          @RequestParam(required = false) Integer year,
                                          @RequestParam(required = false) Integer repayStatus) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(giftRecordService.page(userId, current, size, keyword, direction, year, repayStatus));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<GiftRecord> detail(@PathVariable Long id) {
        GiftRecord record = giftRecordService.getById(id);
        if (record == null) {
            return Result.fail(404, "记录不存在: " + id);
        }
        return Result.ok(record);
    }

    /**
     * 新增
     */
    @PostMapping
    @SaCheckPermission("gift:add")
    public Result<GiftRecord> create(@RequestBody GiftRecord record) {
        validate(record);
        Long userId = StpUtil.getLoginIdAsLong();
        record.setId(null);
        record.setUserId(userId);
        giftRecordService.save(record);
        return Result.ok(record);
    }

    /**
     * 修改
     */
    @PutMapping("/{id}")
    @SaCheckPermission("gift:edit")
    public Result<GiftRecord> update(@PathVariable Long id, @RequestBody GiftRecord record) {
        validate(record);
        GiftRecord exists = giftRecordService.getById(id);
        if (exists == null) {
            return Result.fail(404, "记录不存在: " + id);
        }
        record.setId(id);
        record.setUserId(exists.getUserId());
        giftRecordService.updateById(record);
        return Result.ok(giftRecordService.getById(id));
    }

    private void validate(GiftRecord record) {
        if (!StringUtils.hasText(record.getName())) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (record.getAmount() == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        if (!StringUtils.hasText(record.getOccasion())) {
            throw new IllegalArgumentException("事由不能为空");
        }
        if (record.getGiftDate() == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("gift:delete")
    public Result<Void> delete(@PathVariable Long id) {
        giftRecordService.removeById(id);
        return Result.ok();
    }

    /**
     * 批量删除（逻辑删除）
     * POST /api/gifts/batch-delete  body: { "ids": [1,2,3] }
     */
    @PostMapping("/batch-delete")
    @SaCheckPermission("gift:delete")
    public Result<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("请选择要删除的记录");
        }
        giftRecordService.removeByIds(ids);
        return Result.ok();
    }

    /**
     * 导入礼单（Excel/CSV）
     * POST /api/gifts/import  (multipart/form-data, field name = file)
     */
    @PostMapping("/import")
    @SaCheckPermission("gift:import")
    public Result<ImportResult> importFile(@RequestParam("file") MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(giftRecordService.importFile(userId, file));
    }

    /**
     * 按人往来汇总（分页 + 关键字搜索）
     * GET /api/gifts/contacts?current=&size=&keyword=
     */
    @GetMapping("/contacts")
    public Result<IPage<ContactStat>> contacts(@RequestParam(defaultValue = "1") long current,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) String keyword) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(giftRecordService.contactsStatPage(userId, current, size, keyword));
    }

    /**
     * 下载导入模板
     * GET /api/gifts/template
     */
    @GetMapping("/template")
    public void template(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String filename = URLEncoder.encode("礼单导入模板.xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename*=utf-8''" + filename);

        GiftImportRow example = new GiftImportRow();
        example.setName("张三");
        example.setAmount("600");
        example.setDirection("送出");
        example.setGiftDate("2026-09-01");
        example.setOccasion("结婚");
        example.setRelation("朋友");
        example.setRemark("示例数据");

        EasyExcel.write(response.getOutputStream(), GiftImportRow.class)
                .sheet("礼单")
                .doWrite(List.of(example));
    }
}
