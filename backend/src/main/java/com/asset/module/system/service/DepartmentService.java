package com.asset.module.system.service;

import com.asset.common.exception.BusinessException;
import com.asset.module.system.entity.Department;
import com.asset.module.system.mapper.DepartmentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    
    private final DepartmentMapper departmentMapper;
    
    /**
     * 获取所有启用的部门列表
     */
    public List<Department> getActiveDepartments() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getStatus, 1);
        wrapper.orderByAsc(Department::getSortOrder);
        return departmentMapper.selectList(wrapper);
    }
    
    /**
     * 获取所有部门列表
     */
    public List<Department> getAllDepartments() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Department::getSortOrder);
        return departmentMapper.selectList(wrapper);
    }
    
    /**
     * 创建部门
     */
    public Long createDepartment(Department department) {
        // 检查部门编码是否已存在
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getDeptCode, department.getDeptCode());
        if (departmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("部门编码已存在");
        }
        
        departmentMapper.insert(department);
        return department.getId();
    }
    
    /**
     * 更新部门
     */
    public void updateDepartment(Long id, Department department) {
        Department existing = departmentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("部门不存在");
        }
        
        // 如果修改了部门编码，检查新编码是否已存在
        if (!existing.getDeptCode().equals(department.getDeptCode())) {
            LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Department::getDeptCode, department.getDeptCode());
            wrapper.ne(Department::getId, id);
            if (departmentMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("部门编码已存在");
            }
        }
        
        department.setId(id);
        departmentMapper.updateById(department);
    }
    
    /**
     * 删除部门
     */
    public void deleteDepartment(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }
        
        departmentMapper.deleteById(id);
    }
}
