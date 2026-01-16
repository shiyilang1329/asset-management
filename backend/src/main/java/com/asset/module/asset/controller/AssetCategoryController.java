package com.asset.module.asset.controller;

import com.asset.common.result.Result;
import com.asset.module.asset.entity.AssetCategory;
import com.asset.module.asset.service.AssetCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "资产分类管理")
@RestController
@RequestMapping("/asset/category")
@RequiredArgsConstructor
public class AssetCategoryController {
    
    private final AssetCategoryService categoryService;
    
    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    public Result<List<AssetCategory>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }
    
    @PostMapping
    @Operation(summary = "新增分类")
    public Result<Long> createCategory(@RequestBody AssetCategory category) {
        return Result.success(categoryService.createCategory(category));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody AssetCategory category) {
        categoryService.updateCategory(id, category);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
