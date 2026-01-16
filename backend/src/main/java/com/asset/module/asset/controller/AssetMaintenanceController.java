package com.asset.module.asset.controller;

import com.asset.common.result.PageResult;
import com.asset.common.result.Result;
import com.asset.module.asset.dto.MaintenanceCreateDTO;
import com.asset.module.asset.dto.MaintenanceQueryDTO;
import com.asset.module.asset.dto.MaintenanceVO;
import com.asset.module.asset.service.AssetMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "资产维修管理")
@RestController
@RequestMapping("/asset/maintenance")
@RequiredArgsConstructor
public class AssetMaintenanceController {
    
    private final AssetMaintenanceService maintenanceService;
    
    @PostMapping
    @Operation(summary = "创建维修记录")
    public Result<Long> createMaintenance(@Valid @RequestBody MaintenanceCreateDTO dto) {
        return Result.success(maintenanceService.createMaintenance(dto));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新维修记录")
    public Result<Void> updateMaintenance(@PathVariable Long id, @Valid @RequestBody MaintenanceCreateDTO dto) {
        maintenanceService.updateMaintenance(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除维修记录")
    public Result<Void> deleteMaintenance(@PathVariable Long id) {
        maintenanceService.deleteMaintenance(id);
        return Result.success();
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询维修记录")
    public Result<PageResult<MaintenanceVO>> pageMaintenances(MaintenanceQueryDTO queryDTO) {
        return Result.success(maintenanceService.pageMaintenances(queryDTO));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取维修记录详情")
    public Result<MaintenanceVO> getMaintenanceDetail(@PathVariable Long id) {
        return Result.success(maintenanceService.getMaintenanceDetail(id));
    }
}