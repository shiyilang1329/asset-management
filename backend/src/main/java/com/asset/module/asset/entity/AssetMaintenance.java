package com.asset.module.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("asset_maintenance")
public class AssetMaintenance {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long assetId;
    
    private Long reportUserId;
    
    private LocalDate reportDate;
    
    private String problemDesc;
    
    private LocalDate maintenanceDate;
    
    private BigDecimal maintenanceCost;
    
    private String maintenanceResult;
    
    /**
     * 状态 1:待维修 2:维修中 3:已完成
     */
    private Integer status;
    
    private LocalDateTime createTime;
}