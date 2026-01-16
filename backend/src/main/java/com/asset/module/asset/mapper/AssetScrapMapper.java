package com.asset.module.asset.mapper;

import com.asset.module.asset.dto.AssetScrapVO;
import com.asset.module.asset.entity.AssetScrap;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssetScrapMapper extends BaseMapper<AssetScrap> {
    
    @Select("<script>" +
            "SELECT s.*, a.asset_no, a.asset_name, e.name as operator_name " +
            "FROM asset_scrap s " +
            "LEFT JOIN asset a ON s.asset_id = a.id " +
            "LEFT JOIN employee e ON s.operator_id = e.id " +
            "WHERE 1=1 " +
            "<if test='assetName != null and assetName != \"\"'>" +
            "AND a.asset_name LIKE CONCAT('%', #{assetName}, '%') " +
            "</if>" +
            "<if test='assetNo != null and assetNo != \"\"'>" +
            "AND a.asset_no LIKE CONCAT('%', #{assetNo}, '%') " +
            "</if>" +
            "ORDER BY s.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<AssetScrapVO> selectScrapList(String assetName, String assetNo, int offset, int pageSize);
    
    @Select("<script>" +
            "SELECT COUNT(*) " +
            "FROM asset_scrap s " +
            "LEFT JOIN asset a ON s.asset_id = a.id " +
            "WHERE 1=1 " +
            "<if test='assetName != null and assetName != \"\"'>" +
            "AND a.asset_name LIKE CONCAT('%', #{assetName}, '%') " +
            "</if>" +
            "<if test='assetNo != null and assetNo != \"\"'>" +
            "AND a.asset_no LIKE CONCAT('%', #{assetNo}, '%') " +
            "</if>" +
            "</script>")
    long countScrapList(String assetName, String assetNo);
}
