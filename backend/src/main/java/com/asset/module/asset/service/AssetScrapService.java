package com.asset.module.asset.service;

import com.asset.common.exception.BusinessException;
import com.asset.common.result.PageResult;
import com.asset.module.asset.dto.AssetScrapDTO;
import com.asset.module.asset.dto.AssetScrapQueryDTO;
import com.asset.module.asset.dto.AssetScrapVO;
import com.asset.module.asset.entity.Asset;
import com.asset.module.asset.entity.AssetScrap;
import com.asset.module.asset.mapper.AssetMapper;
import com.asset.module.asset.mapper.AssetScrapMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetScrapService {
    
    private final AssetScrapMapper assetScrapMapper;
    private final AssetMapper assetMapper;
    
    @Transactional
    public Long createScrap(AssetScrapDTO dto) {
        // 检查资产是否存在
        Asset asset = assetMapper.selectById(dto.getAssetId());
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        
        // 检查资产状态
        if (asset.getStatus() == 2) {
            throw new BusinessException("资产正在借出中，无法报废");
        }
        if (asset.getStatus() == 4) {
            throw new BusinessException("资产正在维修中，无法报废");
        }
        if (asset.getStatus() == 5) {
            throw new BusinessException("资产已报废");
        }
        
        // 创建报废记录
        AssetScrap scrap = new AssetScrap();
        scrap.setAssetId(dto.getAssetId());
        scrap.setScrapDate(dto.getScrapDate());
        scrap.setScrapReason(dto.getScrapReason());
        scrap.setOperatorId(1L); // TODO: 从当前登录用户获取
        scrap.setCreateTime(LocalDateTime.now());
        assetScrapMapper.insert(scrap);
        
        // 更新资产状态为报废
        asset.setStatus(5);
        assetMapper.updateById(asset);
        
        return scrap.getId();
    }
    
    public PageResult<AssetScrapVO> pageScrap(AssetScrapQueryDTO queryDTO) {
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        List<AssetScrapVO> records = assetScrapMapper.selectScrapList(
            queryDTO.getAssetName(),
            queryDTO.getAssetNo(),
            offset,
            queryDTO.getPageSize()
        );
        
        long total = assetScrapMapper.countScrapList(
            queryDTO.getAssetName(),
            queryDTO.getAssetNo()
        );
        
        return new PageResult<>(records, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }
    
    @Transactional
    public void deleteScrap(Long id) {
        AssetScrap scrap = assetScrapMapper.selectById(id);
        if (scrap == null) {
            throw new BusinessException("报废记录不存在");
        }
        
        // 删除报废记录
        assetScrapMapper.deleteById(id);
        
        // 恢复资产状态为在库
        Asset asset = assetMapper.selectById(scrap.getAssetId());
        if (asset != null && asset.getStatus() == 5) {
            asset.setStatus(1);
            assetMapper.updateById(asset);
        }
    }
}
