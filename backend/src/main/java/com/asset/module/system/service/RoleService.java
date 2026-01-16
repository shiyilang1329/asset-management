package com.asset.module.system.service;

import com.asset.common.exception.BusinessException;
import com.asset.common.result.PageResult;
import com.asset.module.system.dto.RoleDTO;
import com.asset.module.system.dto.UserRoleDTO;
import com.asset.module.system.entity.SysRole;
import com.asset.module.system.entity.SysUserRole;
import com.asset.module.system.mapper.SysRoleMapper;
import com.asset.module.system.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    
    public Long createRole(RoleDTO dto) {
        // 检查角色编码是否已存在
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode());
        if (sysRoleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }
        
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        role.setCreateTime(LocalDateTime.now());
        sysRoleMapper.insert(role);
        return role.getId();
    }
    
    public PageResult<SysRole> pageRoles(int pageNum, int pageSize) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysRole::getCreateTime);
        Page<SysRole> result = sysRoleMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }
    
    public List<SysRole> getAllRoles() {
        return sysRoleMapper.selectList(null);
    }
    
    public void updateRole(Long id, RoleDTO dto) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        role.setId(id);
        sysRoleMapper.updateById(role);
    }
    
    public void deleteRole(Long id) {
        // 检查是否有用户关联此角色
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleId, id);
        if (sysUserRoleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该角色已分配给用户，无法删除");
        }
        sysRoleMapper.deleteById(id);
    }
    
    public List<SysRole> getUserRoles(Long userId) {
        return sysRoleMapper.selectRolesByUserId(userId);
    }
    
    @Transactional
    public void assignRoles(UserRoleDTO dto) {
        // 删除用户现有角色
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, dto.getUserId());
        sysUserRoleMapper.delete(wrapper);
        
        // 分配新角色
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            for (Long roleId : dto.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(dto.getUserId());
                userRole.setRoleId(roleId);
                userRole.setCreateTime(LocalDateTime.now());
                sysUserRoleMapper.insert(userRole);
            }
        }
    }
}
