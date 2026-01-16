package com.asset.module.system.controller;

import com.asset.common.result.Result;
import com.asset.module.system.entity.Department;
import com.asset.module.system.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentController {
    
    private final DepartmentService departmentService;
    
    @GetMapping("/list")
    @Operation(summary = "获取所有启用的部门列表")
    public Result<List<Department>> getActiveDepartments() {
        return Result.success(departmentService.getActiveDepartments());
    }
    
    @GetMapping("/all")
    @Operation(summary = "获取所有部门列表")
    public Result<List<Department>> getAllDepartments() {
        return Result.success(departmentService.getAllDepartments());
    }
    
    @PostMapping
    @Operation(summary = "创建部门")
    public Result<Long> createDepartment(@Valid @RequestBody Department department) {
        return Result.success(departmentService.createDepartment(department));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新部门")
    public Result<Void> updateDepartment(@PathVariable Long id, @Valid @RequestBody Department department) {
        departmentService.updateDepartment(id, department);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }
}
