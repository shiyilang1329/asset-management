package com.asset.module.asset.dto;

import lombok.Data;

@Data
public class BorrowQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String assetName;      // 资产名称模糊查询
    private String assetModel;     // 资产规格模糊查询（品牌+型号）
    private Integer status;        // 借用状态筛选：0-待生效，1-借出中，2-已归还
}
