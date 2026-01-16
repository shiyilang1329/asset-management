package com.asset.module.asset.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowCreateDTO {
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    
    @NotNull(message = "借用人ID不能为空")
    private Long borrowerId;
    
    @NotNull(message = "借用日期不能为空")
    private LocalDate borrowDate;
    
    private LocalDate expectReturnDate;
    private String borrowReason;
}
