package com.asset.module.asset.controller;

import com.asset.common.result.PageResult;
import com.asset.common.result.Result;
import com.asset.module.asset.dto.AssetScrapDTO;
import com.asset.module.asset.dto.AssetScrapQueryDTO;
import com.asset.module.asset.dto.AssetScrapVO;
import com.asset.module.asset.service.AssetScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asset/scrap")
@RequiredArgsConstructor
public class AssetScrapController {
    
    private final AssetScrapService assetScrapService;
    
    @PostMapping
    public Result<Long> createScrap(@RequestBody AssetScrapDTO dto) {
        Long id = assetScrapService.createScrap(dto);
        return Result.success(id);
    }
    
    @GetMapping("/page")
    public Result<PageResult<AssetScrapVO>> pageScrap(AssetScrapQueryDTO queryDTO) {
        PageResult<AssetScrapVO> result = assetScrapService.pageScrap(queryDTO);
        return Result.success(result);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteScrap(@PathVariable Long id) {
        assetScrapService.deleteScrap(id);
        return Result.success();
    }
}
