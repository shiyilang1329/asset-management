package com.asset.module.asset.service;

import com.asset.common.exception.BusinessException;
import com.asset.common.result.PageResult;
import com.asset.module.asset.dto.AssetBorrowVO;
import com.asset.module.asset.dto.AssetQueryDTO;
import com.asset.module.asset.dto.BorrowCreateDTO;
import com.asset.module.asset.dto.BorrowQueryDTO;
import com.asset.module.asset.entity.Asset;
import com.asset.module.asset.entity.AssetBorrow;
import com.asset.module.asset.mapper.AssetBorrowMapper;
import com.asset.module.asset.mapper.AssetMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetBorrowService {
    
    private final AssetBorrowMapper borrowMapper;
    private final AssetMapper assetMapper;
    
    @Transactional
    public Long createBorrow(BorrowCreateDTO dto) {
        // 检查资产状态
        Asset asset = assetMapper.selectById(dto.getAssetId());
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        
        // 检查资产是否已有未完成的借用记录
        LambdaQueryWrapper<AssetBorrow> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(AssetBorrow::getAssetId, dto.getAssetId())
                   .in(AssetBorrow::getStatus, 1); // 1:借出中
        Long existingCount = borrowMapper.selectCount(checkWrapper);
        if (existingCount > 0) {
            throw new BusinessException("该资产已有借出中的记录，无法重复借用");
        }
        
        // 只有在库状态的资产才能借用
        if (asset.getStatus() != 1) {
            String statusMsg = "";
            switch (asset.getStatus()) {
                case 2: statusMsg = "资产正在借出中"; break;
                case 3: statusMsg = "资产已被预约"; break;
                case 4: statusMsg = "资产正在维修中"; break;
                case 5: statusMsg = "资产已报废"; break;
                default: statusMsg = "资产当前状态不可借用"; break;
            }
            throw new BusinessException(statusMsg + "，无法借用");
        }
        
        // 创建借用记录，直接设置为"借出中"
        AssetBorrow borrow = new AssetBorrow();
        BeanUtils.copyProperties(dto, borrow);
        borrow.setStatus(1); // 借出中
        
        borrowMapper.insert(borrow);
        
        // 更新资产状态为"领用中"
        asset.setStatus(2); // 领用中
        asset.setCurrentUserId(dto.getBorrowerId());
        assetMapper.updateById(asset);
        
        return borrow.getId();
    }
    
    @Transactional
    public void returnAsset(Long borrowId, LocalDate returnDate) {
        AssetBorrow borrow = borrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new BusinessException("借用记录不存在");
        }
        if (borrow.getStatus() == 2) {
            throw new BusinessException("资产已归还");
        }
        
        // 判断归还日期是否是未来日期
        LocalDate today = LocalDate.now();
        boolean isFutureDate = returnDate.isAfter(today);
        
        // 更新借用记录
        borrow.setActualReturnDate(returnDate);
        
        if (isFutureDate) {
            // 如果是未来日期，状态保持为"借出中"，等定时任务自动更新
            borrow.setStatus(1); // 借出中
        } else {
            // 如果是今天或过去的日期，立即更新为"已归还"
            borrow.setStatus(2); // 已归还
            
            // 更新资产状态
            Asset asset = assetMapper.selectById(borrow.getAssetId());
            if (asset != null) {
                asset.setStatus(1); // 在库
                asset.setCurrentUserId(null);
                assetMapper.updateById(asset);
            }
        }
        
        borrowMapper.updateById(borrow);
    }
    
    @Transactional
    public void deleteBorrow(Long borrowId) {
        AssetBorrow borrow = borrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new BusinessException("借用记录不存在");
        }
        
        // 只允许删除"已归还"状态的记录
        if (borrow.getStatus() != 2) {
            throw new BusinessException("只能删除已归还状态的领用记录");
        }
        
        // 删除借用记录
        borrowMapper.deleteById(borrowId);
    }
    
    public PageResult<AssetBorrowVO> pageBorrows(BorrowQueryDTO queryDTO) {
        Page<AssetBorrowVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<AssetBorrowVO> result = borrowMapper.selectBorrowVOPage(
            page, 
            queryDTO.getAssetName(), 
            queryDTO.getAssetModel(), 
            queryDTO.getStatus()
        );
        return new PageResult<>(result.getRecords(), result.getTotal(), 
                                queryDTO.getPageNum(), queryDTO.getPageSize());
    }
    
    public List<Asset> getAvailableAssets() {
        // 获取状态为"在库"的资产（排除预约中、领用中、维修中、报废）
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Asset::getStatus, 1); // 1: 在库（只有在库状态才能借用）
        wrapper.orderByDesc(Asset::getCreateTime);
        return assetMapper.selectList(wrapper);
    }
}
