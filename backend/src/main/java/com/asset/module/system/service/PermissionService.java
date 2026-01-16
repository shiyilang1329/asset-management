package com.asset.module.system.service;

import com.asset.common.exception.BusinessException;
import com.asset.module.system.dto.PermissionDTO;
import com.asset.module.system.dto.PermissionTreeVO;
import com.asset.module.system.entity.SysPermission;
import com.asset.module.system.entity.SysRolePermission;
import com.asset.module.system.mapper.SysPermissionMapper;
import com.asset.module.system.mapper.SysRolePermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    
    public Long createPermission(PermissionDTO dto) {
        // 检查权限编码是否已存在
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getPermissionCode, dto.getPermissionCode());
        if (sysPermissionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("权限编码已存在");
        }
        
        SysPermission permission = new SysPermission();
        BeanUtils.copyProperties(dto, permission);
        permission.setCreateTime(LocalDateTime.now());
        sysPermissionMapper.insert(permission);
        return permission.getId();
    }
    
    public List<PermissionTreeVO> getPermissionTree() {
        List<SysPermission> allPermissions = sysPermissionMapper.selectList(null);
        return buildTree(allPermissions, 0L);
    }
    
    private List<PermissionTreeVO> buildTree(List<SysPermission> permissions, Long parentId) {
        List<PermissionTreeVO> tree = new ArrayList<>();
        for (SysPermission permission : permissions) {
            if (permission.getParentId().equals(parentId)) {
                PermissionTreeVO node = new PermissionTreeVO();
                BeanUtils.copyProperties(permission, node);
                node.setChildren(buildTree(permissions, permission.getId()));
                tree.add(node);
            }
        }
        return tree.stream()
                .sorted((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()))
                .collect(Collectors.toList());
    }
    
    public void updatePermission(Long id, PermissionDTO dto) {
        SysPermission permission = new SysPermission();
        BeanUtils.copyProperties(dto, permission);
        permission.setId(id);
        sysPermissionMapper.updateById(permission);
    }
    
    public void deletePermission(Long id) {
        // 检查是否有子权限
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getParentId, id);
        if (sysPermissionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该权限下有子权限，无法删除");
        }
        
        // 检查是否有角色关联
        LambdaQueryWrapper<SysRolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(SysRolePermission::getPermissionId, id);
        if (sysRolePermissionMapper.selectCount(rpWrapper) > 0) {
            throw new BusinessException("该权限已分配给角色，无法删除");
        }
        
        sysPermissionMapper.deleteById(id);
    }
    
    public List<SysPermission> getUserPermissions(Long userId) {
        return sysPermissionMapper.selectPermissionsByUserId(userId);
    }
    
    public List<SysPermission> getRolePermissions(Long roleId) {
        return sysPermissionMapper.selectPermissionsByRoleId(roleId);
    }
    
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 删除角色现有权限
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        sysRolePermissionMapper.delete(wrapper);
        
        // 分配新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermission.setCreateTime(LocalDateTime.now());
                sysRolePermissionMapper.insert(rolePermission);
            }
        }
    }
}
