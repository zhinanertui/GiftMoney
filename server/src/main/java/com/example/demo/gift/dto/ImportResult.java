package com.example.demo.gift.dto;

import lombok.Data;

/**
 * 导入结果
 */
@Data
public class ImportResult {
    private int success;
    private int failed;
    private String message;

    public static ImportResult of(int success, int failed, String message) {
        ImportResult r = new ImportResult();
        r.setSuccess(success);
        r.setFailed(failed);
        r.setMessage(message);
        return r;
    }
}
