package com.asset.module.asset.service;

import com.asset.common.exception.BusinessException;
import com.asset.common.result.PageResult;
import com.asset.module.asset.dto.MaintenanceCreateDTO;
import com.asset.module.asset.dto.MaintenanceQueryDTO;
import com.asset.module.asset.dto.MaintenanceVO;
import com.asset.module.asset.entity.Asset;
import com.asset.module.asset.entity.AssetMaintenance;
import com.asset.module.asset.mapper.AssetMaintenanceMapper;
import com.asset.module.asset.mapper.AssetMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssetMaintenanceService {
    
    private final AssetMaintenanceMapper maintenanceMapper;
    private final AssetMapper assetMapper;
    
    @Transactional
    public Long createMaintenance(MaintenanceCreateDTO dto) {
        // 检查资产是否存在
        Asset asset = assetMapper.selectById(dto.getAssetId());
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        
        // 检查资产是否已有未完成的维修记录
        LambdaQueryWrapper<AssetMaintenance> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(AssetMaintenance::getAssetId, dto.getAssetId())
                   .in(AssetMaintenance::getStatus, 1, 2); // 1:待维修, 2:维修中
        Long existingCount = maintenanceMapper.selectCount(checkWrapper);
        if (existingCount > 0) {
            throw new BusinessException("该资产已有未完成的维修记录，无法重复提交");
        }
        
        // 创建维修记录
        AssetMaintenance maintenance = new AssetMaintenance();
        BeanUtils.copyProperties(dto, maintenance);
        maintenance.setCreateTime(LocalDateTime.now());
        
        maintenanceMapper.insert(maintenance);
        
        // 根据维修状态更新资产状态
        if (dto.getStatus() == 1 || dto.getStatus() == 2) {
            // 待维修或维修中时，将资产状态设置为维修中（防止被借用或删除）
            asset.setStatus(4); // 4: 维修状态
            assetMapper.updateById(asset);
            System.out.println("维修记录创建，资产状态更新为: 维修中(4)");
        } else if (dto.getStatus() == 3) {
            // 如果直接创建为已完成状态，资产保持在库状态
            System.out.println("维修记录创建为已完成状态，资产状态保持: 在库(1)");
        }
        
        return maintenance.getId();
    }
    
    @Transactional
    public void updateMaintenance(Long id, MaintenanceCreateDTO dto) {
        AssetMaintenance maintenance = maintenanceMapper.selectById(id);
        if (maintenance == null) {
            throw new BusinessException("维修记录不存在");
        }
        
        // 获取原始状态
        Integer oldStatus = maintenance.getStatus();
        
        AssetMaintenance updateMaintenance = new AssetMaintenance();
        BeanUtils.copyProperties(dto, updateMaintenance);
        updateMaintenance.setId(id);
        
        maintenanceMapper.updateById(updateMaintenance);
        
        // 根据状态变化更新资产状态
        Asset asset = assetMapper.selectById(dto.getAssetId());
        if (asset != null) {
            if (dto.getStatus() == 3 && (oldStatus == 1 || oldStatus == 2)) {
                // 从待维修或维修中变为已完成，恢复资产状态为在库
                asset.setStatus(1); // 1: 在库
                assetMapper.updateById(asset);
                System.out.println("维修完成，资产状态恢复为: 在库(1)");
            } else if ((dto.getStatus() == 1 || dto.getStatus() == 2) && oldStatus == 3) {
                // 从已完成变回待维修或维修中，设置为维修状态
                asset.setStatus(4); // 4: 维修中
                assetMapper.updateById(asset);
                System.out.println("维修状态变更，资产状态设置为: 维修中(4)");
            } else if ((dto.getStatus() == 1 || dto.getStatus() == 2) && asset.getStatus() != 4) {
                // 确保待维修或维修中的资产状态为维修中
                asset.setStatus(4); // 4: 维修中
                assetMapper.updateById(asset);
                System.out.println("确保维修中资产状态正确: 维修中(4)");
            }
        }
    }
    
    @Transactional
    public void deleteMaintenance(Long id) {
        AssetMaintenance maintenance = maintenanceMapper.selectById(id);
        if (maintenance == null) {
            throw new BusinessException("维修记录不存在");
        }
        
        // 删除维修记录
        maintenanceMapper.deleteById(id);
        
        // 检查该资产是否还有其他未完成的维修记录
        LambdaQueryWrapper<AssetMaintenance> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(AssetMaintenance::getAssetId, maintenance.getAssetId())
                   .in(AssetMaintenance::getStatus, 1, 2); // 1:待维修, 2:维修中
        Long remainingCount = maintenanceMapper.selectCount(checkWrapper);
        
        // 如果没有其他未完成的维修记录，恢复资产状态
        if (remainingCount == 0) {
            Asset asset = assetMapper.selectById(maintenance.getAssetId());
            if (asset != null && asset.getStatus() == 4) {
                asset.setStatus(1); // 恢复为在库状态
                assetMapper.updateById(asset);
            }
        }
    }
    
    public PageResult<MaintenanceVO> pageMaintenances(MaintenanceQueryDTO queryDTO) {
        Page<MaintenanceVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<MaintenanceVO> result = maintenanceMapper.selectMaintenanceVOPage(
            page, 
            queryDTO.getAssetName(), 
            queryDTO.getStatus()
        );
        return new PageResult<>(result.getRecords(), result.getTotal(), 
                                queryDTO.getPageNum(), queryDTO.getPageSize());
    }
    
    public MaintenanceVO getMaintenanceDetail(Long id) {
        // 这里简化处理，实际应该用联表查询
        AssetMaintenance maintenance = maintenanceMapper.selectById(id);
        if (maintenance == null) {
            throw new BusinessException("维修记录不存在");
        }
        
        MaintenanceVO vo = new MaintenanceVO();
        BeanUtils.copyProperties(maintenance, vo);
        return vo;
    }
}