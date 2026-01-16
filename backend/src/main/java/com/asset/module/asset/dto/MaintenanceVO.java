package com.asset.module.asset.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MaintenanceVO {
    private Long id;
    private Long assetId;
    private Long reportUserId;
    private LocalDate reportDate;
    private String problemDesc;
    private LocalDate maintenanceDate;
    private BigDecimal maintenanceCost;
    private String maintenanceResult;
    private Integer status;
    private LocalDateTime createTime;
    
    // 关联信息
    private String assetNo;
    private String assetName;
    private String assetBrand;
    private String assetModel;
    private String reportUserName;
    private String reportUserPhone;
}