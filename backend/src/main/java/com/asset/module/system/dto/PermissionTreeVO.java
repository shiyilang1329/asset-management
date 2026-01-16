package com.asset.module.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class PermissionTreeVO {
    private Long id;
    private String permissionCode;
    private String permissionName;
    private Integer permissionType;
    private Long parentId;
    private String path;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    private List<PermissionTreeVO> children;
}
