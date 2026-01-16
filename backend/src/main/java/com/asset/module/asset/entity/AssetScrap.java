package com.asset.module.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("asset_scrap")
public class AssetScrap {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private LocalDate scrapDate;
    private String scrapReason;
    private Long operatorId;
    private LocalDateTime createTime;
}
