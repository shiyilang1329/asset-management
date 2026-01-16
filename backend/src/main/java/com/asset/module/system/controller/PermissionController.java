package com.asset.module.system.controller;

import com.asset.common.result.Result;
import com.asset.module.system.dto.PermissionDTO;
import com.asset.module.system.dto.PermissionTreeVO;
import com.asset.module.system.entity.SysPermission;
import com.asset.module.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/permission")
@RequiredArgsConstructor
public class PermissionController {
    
    private final PermissionService permissionService;
    
    @PostMapping
    public Result<Long> createPermission(@RequestBody PermissionDTO dto) {
        Long id = permissionService.createPermission(dto);
        return Result.success(id);
    }
    
    @GetMapping("/tree")
    public Result<List<PermissionTreeVO>> getPermissionTree() {
        List<PermissionTreeVO> tree = permissionService.getPermissionTree();
        return Result.success(tree);
    }
    
    @PutMapping("/{id}")
    public Result<Void> updatePermission(@PathVariable Long id, @RequestBody PermissionDTO dto) {
        permissionService.updatePermission(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success();
    }
    
    @GetMapping("/user/{userId}")
    public Result<List<SysPermission>> getUserPermissions(@PathVariable Long userId) {
        List<SysPermission> permissions = permissionService.getUserPermissions(userId);
        return Result.success(permissions);
    }
    
    @GetMapping("/role/{roleId}")
    public Result<List<SysPermission>> getRolePermissions(@PathVariable Long roleId) {
        List<SysPermission> permissions = permissionService.getRolePermissions(roleId);
        return Result.success(permissions);
    }
    
    @PostMapping("/assign")
    public Result<Void> assignPermissions(@RequestBody Map<String, Object> params) {
        Long roleId = Long.valueOf(params.get("roleId").toString());
        @SuppressWarnings("unchecked")
        List<Object> permissionIdsObj = (List<Object>) params.get("permissionIds");
        List<Long> permissionIds = permissionIdsObj.stream()
                .map(obj -> Long.valueOf(obj.toString()))
                .collect(java.util.stream.Collectors.toList());
        permissionService.assignPermissions(roleId, permissionIds);
        return Result.success();
    }
}
