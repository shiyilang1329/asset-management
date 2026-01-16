package com.asset.module.employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeFormDTO {
    private String employeeNo;
    
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    private String phone;
    private String email;
    private String department;
    private String position;
    private Integer status;
}
