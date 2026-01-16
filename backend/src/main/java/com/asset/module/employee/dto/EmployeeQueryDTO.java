package com.asset.module.employee.dto;

import lombok.Data;

@Data
public class EmployeeQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String name;
    private String phone;
    private String department;
    private Integer status;
}
