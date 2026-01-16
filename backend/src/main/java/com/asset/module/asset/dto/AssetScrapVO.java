package com.asset.module.asset.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AssetScrapVO {
    private Long id;
    private Long assetId;
    private String assetNo;
    private String assetName;
    private LocalDate scrapDate;
    private String scrapReason;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createTime;
}
