package com.asset.module.asset.mapper;

import com.asset.module.asset.entity.Asset;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssetMapper extends BaseMapper<Asset> {
    
    @Select("SELECT COUNT(*) > 0 FROM asset_borrow WHERE asset_id = #{assetId} AND status = 1")
    boolean hasActiveBorrow(Long assetId);
    
    @Select("SELECT COUNT(*) > 0 FROM asset_maintenance WHERE asset_id = #{assetId} AND status IN (1, 2)")
    boolean hasActiveMaintenance(Long assetId);
}
