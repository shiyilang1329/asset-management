package com.asset.module.asset.mapper;

import com.asset.module.asset.dto.MaintenanceVO;
import com.asset.module.asset.entity.AssetMaintenance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssetMaintenanceMapper extends BaseMapper<AssetMaintenance> {
    
    /**
     * 分页查询维修记录（包含资产和用户信息）
     * 支持资产名称模糊查询和状态筛选
     */
    @Select("<script>" +
            "SELECT " +
            "m.id, m.asset_id, m.report_user_id, " +
            "m.report_date, m.problem_desc, m.maintenance_date, " +
            "m.maintenance_cost, m.maintenance_result, m.status, m.create_time, " +
            "COALESCE(a.asset_no, '已删除') as asset_no, " +
            "COALESCE(a.asset_name, '资产已删除') as asset_name, " +
            "COALESCE(a.brand, '') as asset_brand, " +
            "COALESCE(a.model, '') as asset_model, " +
            "COALESCE(e.name, '人员已删除') as report_user_name, " +
            "COALESCE(e.phone, '') as report_user_phone " +
            "FROM asset_maintenance m " +
            "LEFT JOIN asset a ON m.asset_id = a.id " +
            "LEFT JOIN employee e ON m.report_user_id = e.id " +
            "WHERE 1=1 " +
            "<if test='assetName != null and assetName != \"\"'>" +
            "AND a.asset_name LIKE CONCAT('%', #{assetName}, '%') " +
            "</if>" +
            "<if test='status != null'>" +
            "AND m.status = #{status} " +
            "</if>" +
            "ORDER BY m.create_time DESC" +
            "</script>")
    Page<MaintenanceVO> selectMaintenanceVOPage(Page<MaintenanceVO> page, 
                                               @Param("assetName") String assetName,
                                               @Param("status") Integer status);
}