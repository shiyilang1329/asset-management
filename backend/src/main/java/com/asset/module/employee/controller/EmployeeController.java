package com.asset.module.employee.controller;

import com.asset.common.result.PageResult;
import com.asset.common.result.Result;
import com.asset.common.utils.ExcelTemplateUtil;
import com.asset.module.employee.dto.EmployeeFormDTO;
import com.asset.module.employee.dto.EmployeeQueryDTO;
import com.asset.module.employee.entity.Employee;
import com.asset.module.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "人员管理")
@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @GetMapping("/list")
    @Operation(summary = "获取员工列表")
    public Result<List<Employee>> getEmployeeList() {
        return Result.success(employeeService.getEmployeeList());
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询员工")
    public Result<PageResult<Employee>> pageEmployees(EmployeeQueryDTO queryDTO) {
        return Result.success(employeeService.pageEmployees(queryDTO));
    }
    
    @PostMapping
    @Operation(summary = "创建员工")
    public Result<Long> createEmployee(@Valid @RequestBody EmployeeFormDTO dto) {
        return Result.success(employeeService.createEmployee(dto));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新员工")
    public Result<Void> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeFormDTO dto) {
        employeeService.updateEmployee(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除员工")
    public Result<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return Result.success();
    }
    
    @PostMapping("/import")
    @Operation(summary = "批量导入员工")
    public Result<Map<String, Object>> importEmployees(@RequestParam("file") MultipartFile file) {
        return Result.success(employeeService.importEmployees(file));
    }
    
    @GetMapping("/template")
    @Operation(summary = "下载员工导入模板")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        byte[] data = ExcelTemplateUtil.generateEmployeeTemplate();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=employee_template.xlsx");
        response.getOutputStream().write(data);
    }
}
