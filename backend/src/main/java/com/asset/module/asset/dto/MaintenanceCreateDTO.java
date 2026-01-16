package com.asset.module.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MaintenanceCreateDTO {
    
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    
    @NotNull(message = "报修人ID不能为空")
    private Long reportUserId;
    
    @NotNull(message = "报修日期不能为空")
    private LocalDate reportDate;
    
    @NotBlank(message = "问题描述不能为空")
    private String problemDesc;
    
    private LocalDate maintenanceDate;
    
    private BigDecimal maintenanceCost;
    
    private String maintenanceResult;
    
    private Integer status = 1; // 默认待维修
}