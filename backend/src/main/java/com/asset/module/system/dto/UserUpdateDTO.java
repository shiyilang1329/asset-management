package com.asset.module.system.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String realName;
    private String email;
    private String phone;
    private Long departmentId;
    private Integer status;
}
