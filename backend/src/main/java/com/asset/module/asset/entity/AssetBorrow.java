package com.asset.module.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("asset_borrow")
public class AssetBorrow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private Long borrowerId;
    private LocalDate borrowDate;
    private LocalDate expectReturnDate;
    private LocalDate actualReturnDate;
    private String borrowReason;
    private Integer status;
    private LocalDateTime createTime;
}
