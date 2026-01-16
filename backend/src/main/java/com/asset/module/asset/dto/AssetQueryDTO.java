package com.asset.module.asset.dto;

import lombok.Data;

@Data
public class AssetQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String assetName;
    private String assetNo;
    private Long categoryId;
    private Integer status;
}
