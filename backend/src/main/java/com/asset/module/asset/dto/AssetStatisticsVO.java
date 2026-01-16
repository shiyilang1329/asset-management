package com.asset.module.asset.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetStatisticsVO {
    private Long totalCount;
    private Long inStockCount;
    private Long borrowedCount;
    private Long maintenanceCount;
    private Long scrapCount;
}
