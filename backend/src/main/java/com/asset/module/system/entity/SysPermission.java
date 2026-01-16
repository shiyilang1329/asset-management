package com.asset.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
public class SysPermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String permissionCode;
    private String permissionName;
    private Integer permissionType; // 1:菜单 2:按钮
    private Long parentId;
    private String path;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
}
