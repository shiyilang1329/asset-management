package com.asset.module.employee.service;

import com.asset.common.exception.BusinessException;
import com.asset.common.result.PageResult;
import com.asset.common.utils.ExcelUtil;
import com.asset.module.employee.dto.EmployeeFormDTO;
import com.asset.module.employee.dto.EmployeeQueryDTO;
import com.asset.module.employee.entity.Employee;
import com.asset.module.employee.mapper.EmployeeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    
    private final EmployeeMapper employeeMapper;
    
    public List<Employee> getEmployeeList() {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getStatus, 1); // 只返回在职员工
        wrapper.orderByAsc(Employee::getName);
        return employeeMapper.selectList(wrapper);
    }
    
    public PageResult<Employee> pageEmployees(EmployeeQueryDTO queryDTO) {
        Page<Employee> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(Employee::getName, queryDTO.getName());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.like(Employee::getPhone, queryDTO.getPhone());
        }
        if (StringUtils.hasText(queryDTO.getDepartment())) {
            wrapper.like(Employee::getDepartment, queryDTO.getDepartment());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Employee::getStatus, queryDTO.getStatus());
        }
        
        wrapper.orderByDesc(Employee::getCreateTime);
        Page<Employee> result = employeeMapper.selectPage(page, wrapper);
        
        return new PageResult<>(result.getRecords(), result.getTotal(), 
                                queryDTO.getPageNum(), queryDTO.getPageSize());
    }
    
    public Long createEmployee(EmployeeFormDTO dto) {
        // 检查工号是否已存在
        if (StringUtils.hasText(dto.getEmployeeNo())) {
            LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Employee::getEmployeeNo, dto.getEmployeeNo());
            if (employeeMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("工号已存在");
            }
        }
        
        Employee employee = new Employee();
        BeanUtils.copyProperties(dto, employee);
        employee.setStatus(1); // 默认在职
        employee.setCreateTime(LocalDateTime.now());
        
        employeeMapper.insert(employee);
        return employee.getId();
    }
    
    public void updateEmployee(Long id, EmployeeFormDTO dto) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        
        // 检查工号是否重复
        if (StringUtils.hasText(dto.getEmployeeNo()) && !dto.getEmployeeNo().equals(employee.getEmployeeNo())) {
            LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Employee::getEmployeeNo, dto.getEmployeeNo());
            if (employeeMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("工号已存在");
            }
        }
        
        BeanUtils.copyProperties(dto, employee);
        employeeMapper.updateById(employee);
    }
    
    public void deleteEmployee(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        
        // 软删除：设置状态为离职
        employee.setStatus(0);
        employeeMapper.updateById(employee);
    }
    
    public Map<String, Object> importEmployees(MultipartFile file) {
        try {
            List<List<String>> rows = ExcelUtil.readExcel(file);
            
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMsg = new StringBuilder();
            
            for (int i = 0; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                if (row.size() < 2) {
                    failCount++;
                    errorMsg.append("第").append(i + 2).append("行数据不完整; ");
                    continue;
                }
                
                try {
                    String employeeNo = row.get(0);
                    String name = row.get(1);
                    String phone = row.size() > 2 ? row.get(2) : "";
                    String email = row.size() > 3 ? row.get(3) : "";
                    String department = row.size() > 4 ? row.get(4) : "";
                    String position = row.size() > 5 ? row.get(5) : "";
                    
                    // 检查工号是否已存在
                    if (StringUtils.hasText(employeeNo)) {
                        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
                        wrapper.eq(Employee::getEmployeeNo, employeeNo);
                        if (employeeMapper.selectCount(wrapper) > 0) {
                            failCount++;
                            errorMsg.append("第").append(i + 2).append("行工号已存在; ");
                            continue;
                        }
                    }
                    
                    // 创建员工
                    Employee employee = new Employee();
                    employee.setEmployeeNo(employeeNo);
                    employee.setName(name);
                    employee.setPhone(phone);
                    employee.setEmail(email);
                    employee.setDepartment(department);
                    employee.setPosition(position);
                    employee.setStatus(1);
                    employee.setCreateTime(LocalDateTime.now());
                    
                    employeeMapper.insert(employee);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errorMsg.append("第").append(i + 2).append("行导入失败: ").append(e.getMessage()).append("; ");
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("total", rows.size());
            result.put("success", successCount);
            result.put("fail", failCount);
            result.put("message", errorMsg.toString());
            
            return result;
        } catch (Exception e) {
            throw new BusinessException("导入失败: " + e.getMessage());
        }
    }
}
