package com.asset.module.asset.controller;

import com.asset.common.result.PageResult;
import com.asset.common.result.Result;
import com.asset.common.utils.ExcelTemplateUtil;
import com.asset.module.asset.dto.AssetCreateDTO;
import com.asset.module.asset.dto.AssetQueryDTO;
import com.asset.module.asset.dto.AssetStatisticsVO;
import com.asset.module.asset.entity.Asset;
import com.asset.module.asset.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Tag(name = "资产管理")
@RestController
@RequestMapping("/asset")
@RequiredArgsConstructor
public class AssetController {
    
    private final AssetService assetService;
    
    @PostMapping
    @Operation(summary = "新增资产")
    public Result<Long> createAsset(@Valid @RequestBody AssetCreateDTO dto) {
        return Result.success(assetService.createAsset(dto));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取资产详情")
    public Result<Asset> getAsset(@PathVariable Long id) {
        return Result.success(assetService.getAssetDetail(id));
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询资产")
    public Result<PageResult<Asset>> pageAssets(AssetQueryDTO queryDTO) {
        return Result.success(assetService.pageAssets(queryDTO));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新资产信息")
    public Result<Void> updateAsset(@PathVariable Long id, @Valid @RequestBody AssetCreateDTO dto) {
        assetService.updateAsset(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除资产")
    public Result<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return Result.success();
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取资产统计数据")
    public Result<AssetStatisticsVO> getStatistics() {
        return Result.success(assetService.getStatistics());
    }
    
    @PostMapping("/import")
    @Operation(summary = "批量导入资产")
    public Result<Map<String, Object>> importAssets(@RequestParam("file") MultipartFile file) {
        return Result.success(assetService.importAssets(file));
    }
    
    @GetMapping("/template")
    @Operation(summary = "下载资产导入模板")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        byte[] data = ExcelTemplateUtil.generateAssetTemplate();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=asset_template.xlsx");
        response.getOutputStream().write(data);
    }
    
    @GetMapping("/available-for-maintenance")
    @Operation(summary = "获取可维修的资产列表（排除报废资产）")
    public Result<PageResult<Asset>> getAvailableForMaintenance(AssetQueryDTO queryDTO) {
        return Result.success(assetService.getAvailableForMaintenance(queryDTO));
    }
}
