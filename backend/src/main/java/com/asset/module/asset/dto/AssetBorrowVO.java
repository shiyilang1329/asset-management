package com.asset.module.asset.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产借用视图对象
 * 包含关联的资产和用户信息
 */
@Data
public class AssetBorrowVO {
    private Long id;
    private Long assetId;
    private Long borrowerId;
    
    // 资产信息
    private String assetNo;          // 资产编号
    private String assetName;        // 资产名称
    private String assetBrand;       // 资产品牌
    private String assetModel;       // 资产型号
    
    // 借用人信息
    private String borrowerUsername; // 借用人用户名
    private String borrowerRealName; // 借用人真实姓名
    private String borrowerPhone;    // 借用人电话
    
    // 借用信息
    private LocalDate borrowDate;
    private LocalDate expectReturnDate;
    private LocalDate actualReturnDate;
    private String borrowReason;
    private Integer status;
    private LocalDateTime createTime;
}
