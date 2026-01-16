package com.asset.module.asset.service;

import com.asset.common.exception.BusinessException;
import com.asset.common.result.PageResult;
import com.asset.common.utils.ExcelUtil;
import com.asset.module.asset.dto.AssetCreateDTO;
import com.asset.module.asset.dto.AssetQueryDTO;
import com.asset.module.asset.dto.AssetStatisticsVO;
import com.asset.module.asset.entity.Asset;
import com.asset.module.asset.mapper.AssetMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssetService {
    
    private final AssetMapper assetMapper;
    
    public Long createAsset(AssetCreateDTO dto) {
        Asset asset = new Asset();
        BeanUtils.copyProperties(dto, asset);
        asset.setStatus(1); // 默认在库状态
        assetMapper.insert(asset);
        return asset.getId();
    }
    
    public Asset getAssetDetail(Long id) {
        return assetMapper.selectById(id);
    }
    
    public PageResult<Asset> pageAssets(AssetQueryDTO queryDTO) {
        Page<Asset> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getAssetName())) {
            wrapper.like(Asset::getAssetName, queryDTO.getAssetName());
        }
        if (StringUtils.hasText(queryDTO.getAssetNo())) {
            wrapper.like(Asset::getAssetNo, queryDTO.getAssetNo());
        }
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Asset::getCategoryId, queryDTO.getCategoryId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Asset::getStatus, queryDTO.getStatus());
        }
        
        wrapper.orderByDesc(Asset::getCreateTime);
        Page<Asset> result = assetMapper.selectPage(page, wrapper);
        
        // 查询每个资产的借用和维修状态
        for (Asset asset : result.getRecords()) {
            // 检查是否有进行中的借用记录
            boolean isBorrowed = assetMapper.hasActiveBorrow(asset.getId());
            // 检查是否有进行中的维修记录
            boolean isMaintenance = assetMapper.hasActiveMaintenance(asset.getId());
            
            // 将状态信息存储在remark字段中（临时方案）
            // 格式：borrowed:true,maintenance:false
            asset.setRemark(String.format("borrowed:%s,maintenance:%s", isBorrowed, isMaintenance));
        }
        
        return new PageResult<>(result.getRecords(), result.getTotal(), 
                                queryDTO.getPageNum(), queryDTO.getPageSize());
    }
    
    public void updateAsset(Long id, AssetCreateDTO dto) {
        Asset asset = new Asset();
        BeanUtils.copyProperties(dto, asset);
        asset.setId(id);
        assetMapper.updateById(asset);
    }
    
    public void deleteAsset(Long id) {
        // 检查资产状态
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        
        // 只有"在库"状态的资产才能删除
        if (asset.getStatus() == 2) {
            throw new BusinessException("资产正在借出中，无法删除");
        }
        if (asset.getStatus() == 3) {
            throw new BusinessException("资产已被预约，无法删除");
        }
        if (asset.getStatus() == 4) {
            throw new BusinessException("资产正在维修中，无法删除");
        }
        if (asset.getStatus() == 5) {
            throw new BusinessException("资产已报废，无法删除");
        }
        
        assetMapper.deleteById(id);
    }
    
    public AssetStatisticsVO getStatistics() {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        Long totalCount = assetMapper.selectCount(wrapper);
        
        wrapper.clear();
        wrapper.eq(Asset::getStatus, 1);
        Long inStockCount = assetMapper.selectCount(wrapper);
        
        wrapper.clear();
        wrapper.eq(Asset::getStatus, 2);
        Long borrowedCount = assetMapper.selectCount(wrapper);
        
        wrapper.clear();
        wrapper.eq(Asset::getStatus, 4);
        Long maintenanceCount = assetMapper.selectCount(wrapper);
        
        wrapper.clear();
        wrapper.eq(Asset::getStatus, 5);
        Long scrapCount = assetMapper.selectCount(wrapper);
        
        return new AssetStatisticsVO(totalCount, inStockCount, borrowedCount, maintenanceCount, scrapCount);
    }
    
    public Map<String, Object> importAssets(MultipartFile file) {
        try {
            List<List<String>> rows = ExcelUtil.readExcel(file);
            
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMsg = new StringBuilder();
            
            for (int i = 0; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                if (row.size() < 3) {
                    failCount++;
                    errorMsg.append("第").append(i + 2).append("行数据不完整; ");
                    continue;
                }
                
                try {
                    String assetNo = row.get(0);
                    String assetName = row.get(1);
                    String categoryIdStr = row.get(2);
                    String brand = row.size() > 3 ? row.get(3) : "";
                    String model = row.size() > 4 ? row.get(4) : "";
                    String priceStr = row.size() > 5 ? row.get(5) : "0";
                    String dateStr = row.size() > 6 ? row.get(6) : "";
                    String supplier = row.size() > 7 ? row.get(7) : "";
                    String location = row.size() > 8 ? row.get(8) : "";
                    
                    // 检查资产编号是否已存在
                    LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Asset::getAssetNo, assetNo);
                    if (assetMapper.selectCount(wrapper) > 0) {
                        failCount++;
                        errorMsg.append("第").append(i + 2).append("行资产编号已存在; ");
                        continue;
                    }
                    
                    // 创建资产
                    Asset asset = new Asset();
                    asset.setAssetNo(assetNo);
                    asset.setAssetName(assetName);
                    asset.setCategoryId(Long.parseLong(categoryIdStr));
                    asset.setBrand(brand);
                    asset.setModel(model);
                    asset.setPurchasePrice(new BigDecimal(priceStr));
                    if (!dateStr.isEmpty()) {
                        asset.setPurchaseDate(LocalDate.parse(dateStr));
                    }
                    asset.setSupplier(supplier);
                    asset.setLocation(location);
                    asset.setStatus(1); // 默认在库
                    asset.setCreateTime(LocalDateTime.now());
                    
                    assetMapper.insert(asset);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errorMsg.append("第").append(i + 2).append("行导入失败: ").append(e.getMessage()).append("; ");
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("total", rows.size());
            result.put("success", successCount);
            result.put("fail", failCount);
            result.put("message", errorMsg.toString());
            
            return result;
        } catch (Exception e) {
            throw new BusinessException("导入失败: " + e.getMessage());
        }
    }
    
    public PageResult<Asset> getAvailableForMaintenance(AssetQueryDTO queryDTO) {
        Page<Asset> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        
        // 排除报废资产
        wrapper.ne(Asset::getStatus, 5);
        
        if (StringUtils.hasText(queryDTO.getAssetName())) {
            wrapper.like(Asset::getAssetName, queryDTO.getAssetName());
        }
        if (StringUtils.hasText(queryDTO.getAssetNo())) {
            wrapper.like(Asset::getAssetNo, queryDTO.getAssetNo());
        }
        
        wrapper.orderByDesc(Asset::getCreateTime);
        Page<Asset> result = assetMapper.selectPage(page, wrapper);
        
        return new PageResult<>(result.getRecords(), result.getTotal(), 
                                queryDTO.getPageNum(), queryDTO.getPageSize());
    }
}
