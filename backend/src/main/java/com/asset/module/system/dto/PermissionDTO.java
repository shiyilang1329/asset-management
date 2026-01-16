package com.asset.module.system.dto;

import lombok.Data;

@Data
public class PermissionDTO {
    private String permissionCode;
    private String permissionName;
    private Integer permissionType;
    private Long parentId;
    private String path;
    private String icon;
    private Integer sortOrder;
    private Integer status;
}
