package com.asset.module.asset.mapper;

import com.asset.module.asset.dto.AssetBorrowVO;
import com.asset.module.asset.entity.AssetBorrow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssetBorrowMapper extends BaseMapper<AssetBorrow> {
    
    /**
     * 分页查询借用记录（包含资产和用户信息）
     * 使用COALESCE处理资产或用户被删除的情况
     * 支持资产名称、资产规格模糊查询和状态筛选
     */
    @Select("<script>" +
            "SELECT " +
            "b.id, b.asset_id, b.borrower_id, " +
            "b.borrow_date, b.expect_return_date, b.actual_return_date, " +
            "b.borrow_reason, b.status, b.create_time, " +
            "COALESCE(a.asset_no, '已删除') as asset_no, " +
            "COALESCE(a.asset_name, '资产已删除') as asset_name, " +
            "COALESCE(a.brand, '') as asset_brand, " +
            "COALESCE(a.model, '') as asset_model, " +
            "COALESCE(e.name, '人员已删除') as borrower_real_name, " +
            "COALESCE(e.phone, '') as borrower_phone " +
            "FROM asset_borrow b " +
            "LEFT JOIN asset a ON b.asset_id = a.id " +
            "LEFT JOIN employee e ON b.borrower_id = e.id " +
            "WHERE 1=1 " +
            "<if test='assetName != null and assetName != \"\"'>" +
            "AND a.asset_name LIKE CONCAT('%', #{assetName}, '%') " +
            "</if>" +
            "<if test='assetModel != null and assetModel != \"\"'>" +
            "AND (a.brand LIKE CONCAT('%', #{assetModel}, '%') OR a.model LIKE CONCAT('%', #{assetModel}, '%')) " +
            "</if>" +
            "<if test='status != null'>" +
            "AND b.status = #{status} " +
            "</if>" +
            "ORDER BY b.create_time DESC" +
            "</script>")
    Page<AssetBorrowVO> selectBorrowVOPage(Page<AssetBorrowVO> page, 
                                           @Param("assetName") String assetName,
                                           @Param("assetModel") String assetModel,
                                           @Param("status") Integer status);
}
