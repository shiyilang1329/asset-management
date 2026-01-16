package com.asset.module.asset.controller;

import com.asset.common.result.PageResult;
import com.asset.common.result.Result;
import com.asset.module.asset.dto.AssetBorrowVO;
import com.asset.module.asset.dto.AssetQueryDTO;
import com.asset.module.asset.dto.BorrowCreateDTO;
import com.asset.module.asset.dto.BorrowQueryDTO;
import com.asset.module.asset.entity.Asset;
import com.asset.module.asset.service.AssetBorrowService;
import com.asset.module.asset.task.BorrowStatusTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "资产领用管理")
@RestController
@RequestMapping("/asset/borrow")
@RequiredArgsConstructor
public class AssetBorrowController {
    
    private final AssetBorrowService borrowService;
    private final BorrowStatusTask borrowStatusTask;
    
    @PostMapping
    @Operation(summary = "创建领用记录")
    public Result<Long> createBorrow(@Valid @RequestBody BorrowCreateDTO dto) {
        return Result.success(borrowService.createBorrow(dto));
    }
    
    @PostMapping("/{id}/return")
    @Operation(summary = "归还资产")
    public Result<Void> returnAsset(@PathVariable Long id, @RequestParam String returnDate) {
        borrowService.returnAsset(id, java.time.LocalDate.parse(returnDate));
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除领用记录（仅限已归还状态）")
    public Result<Void> deleteBorrow(@PathVariable Long id) {
        borrowService.deleteBorrow(id);
        return Result.success();
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询领用记录")
    public Result<PageResult<AssetBorrowVO>> pageBorrows(BorrowQueryDTO queryDTO) {
        return Result.success(borrowService.pageBorrows(queryDTO));
    }
    
    @GetMapping("/available-assets")
    @Operation(summary = "获取可领用的资产列表")
    public Result<List<Asset>> getAvailableAssets() {
        return Result.success(borrowService.getAvailableAssets());
    }
    
    @PostMapping("/update-status")
    @Operation(summary = "手动更新领用状态（将待生效的记录更新为借出中）")
    public Result<Void> updateBorrowStatus() {
        borrowStatusTask.manualUpdate();
        return Result.success();
    }
}
