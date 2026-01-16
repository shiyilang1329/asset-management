package com.asset.module.asset.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AssetScrapDTO {
    private Long assetId;
    private LocalDate scrapDate;
    private String scrapReason;
}
