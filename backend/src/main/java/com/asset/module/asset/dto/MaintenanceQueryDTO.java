package com.asset.module.asset.dto;

import lombok.Data;

@Data
public class MaintenanceQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String assetName;      // 资产名称模糊查询
    private Integer status;        // 维修状态筛选：1-待维修，2-维修中，3-已完成
}