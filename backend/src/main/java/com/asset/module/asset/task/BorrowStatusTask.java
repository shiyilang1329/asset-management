package com.asset.module.asset.task;

import com.asset.module.asset.entity.Asset;
import com.asset.module.asset.entity.AssetBorrow;
import com.asset.module.asset.mapper.AssetBorrowMapper;
import com.asset.module.asset.mapper.AssetMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 领用状态定时任务
 * 每天凌晨1点检查并更新"待生效"的领用记录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BorrowStatusTask {
    
    private final AssetBorrowMapper borrowMapper;
    private final AssetMapper assetMapper;
    
    /**
     * 每天凌晨1点执行
     * 将借用日期是今天或之前的"待生效"记录更新为"借出中"
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void updateBorrowStatus() {
        log.info("开始执行领用状态更新任务...");
        
        try {
            LocalDate today = LocalDate.now();
            
            // 查询所有"待生效"的记录
            LambdaQueryWrapper<AssetBorrow> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AssetBorrow::getStatus, 0); // 待生效
            List<AssetBorrow> pendingBorrows = borrowMapper.selectList(wrapper);
            
            int updatedCount = 0;
            for (AssetBorrow borrow : pendingBorrows) {
                LocalDate borrowDate = borrow.getBorrowDate();
                
                // 如果借用日期是今天或之前，更新为"借出中"
                if (!borrowDate.isAfter(today)) {
                    log.info("更新领用记录 ID={}, 借用日期={}, 状态: 待生效 -> 借出中", 
                            borrow.getId(), borrowDate);
                    
                    // 更新领用记录状态
                    borrow.setStatus(1); // 借出中
                    borrowMapper.updateById(borrow);
                    
                    // 更新资产状态：从"预约中"变为"领用"
                    Asset asset = assetMapper.selectById(borrow.getAssetId());
                    if (asset != null) {
                        asset.setStatus(2); // 领用状态
                        asset.setCurrentUserId(borrow.getBorrowerId());
                        assetMapper.updateById(asset);
                        log.info("更新资产 ID={}, 状态: 预约中 -> 领用", asset.getId());
                    }
                    
                    updatedCount++;
                }
            }
            
            log.info("领用状态更新任务完成，共更新 {} 条记录", updatedCount);
        } catch (Exception e) {
            log.error("领用状态更新任务执行失败", e);
        }
    }
    
    /**
     * 手动触发状态更新（用于测试）
     * 可以通过接口调用此方法
     */
    public void manualUpdate() {
        log.info("手动触发领用状态更新...");
        updateBorrowStatus();
    }
}
