package com.asset.module.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("employee")
public class Employee {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String employeeNo;
    private String name;
    private String phone;
    private String email;
    private String department;
    private String position;
    private Integer status;
    private LocalDateTime createTime;
}
