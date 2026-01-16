package com.asset.module.asset.service;

import com.asset.module.asset.entity.AssetCategory;
import com.asset.module.asset.mapper.AssetCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetCategoryService {
    
    private final AssetCategoryMapper categoryMapper;
    
    public List<AssetCategory> getCategoryTree() {
        LambdaQueryWrapper<AssetCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AssetCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }
    
    public Long createCategory(AssetCategory category) {
        categoryMapper.insert(category);
        return category.getId();
    }
    
    public void updateCategory(Long id, AssetCategory category) {
        category.setId(id);
        categoryMapper.updateById(category);
    }
    
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }
}
