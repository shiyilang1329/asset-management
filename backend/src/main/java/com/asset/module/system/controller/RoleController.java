package com.asset.module.system.controller;

import com.asset.common.result.PageResult;
import com.asset.common.result.Result;
import com.asset.module.system.dto.RoleDTO;
import com.asset.module.system.dto.UserRoleDTO;
import com.asset.module.system.entity.SysRole;
import com.asset.module.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    @PostMapping
    public Result<Long> createRole(@RequestBody RoleDTO dto) {
        Long id = roleService.createRole(dto);
        return Result.success(id);
    }
    
    @GetMapping("/page")
    public Result<PageResult<SysRole>> pageRoles(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<SysRole> result = roleService.pageRoles(pageNum, pageSize);
        return Result.success(result);
    }
    
    @GetMapping("/all")
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> roles = roleService.getAllRoles();
        return Result.success(roles);
    }
    
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody RoleDTO dto) {
        roleService.updateRole(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }
    
    @GetMapping("/user/{userId}")
    public Result<List<SysRole>> getUserRoles(@PathVariable Long userId) {
        List<SysRole> roles = roleService.getUserRoles(userId);
        return Result.success(roles);
    }
    
    @PostMapping("/assign")
    public Result<Void> assignRoles(@RequestBody UserRoleDTO dto) {
        roleService.assignRoles(dto);
        return Result.success();
    }
}
