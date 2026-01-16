package com.asset.module.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("asset")
public class Asset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String assetNo;
    private String assetName;
    private Long categoryId;
    private String brand;
    private String model;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
    private String supplier;
    private Integer status;
    private Long currentUserId;
    private Long currentDepartmentId;
    private String location;
    private String remark;
    private LocalDateTime createTime;
}
