package com.asset.module.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetCreateDTO {
    @NotBlank(message = "资产编号不能为空")
    private String assetNo;
    
    @NotBlank(message = "资产名称不能为空")
    private String assetName;
    
    @NotNull(message = "资产分类不能为空")
    private Long categoryId;
    
    private String brand;
    private String model;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
    private String supplier;
    private String location;
    private String remark;
}
