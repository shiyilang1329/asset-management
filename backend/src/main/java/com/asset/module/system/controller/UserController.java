package com.asset.module.system.controller;

import com.asset.common.result.PageResult;
import com.asset.common.result.Result;
import com.asset.common.utils.ExcelTemplateUtil;
import com.asset.module.system.dto.UserCreateDTO;
import com.asset.module.system.dto.UserQueryDTO;
import com.asset.module.system.dto.UserUpdateDTO;
import com.asset.module.system.entity.SysUser;
import com.asset.module.system.service.UserService;
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

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/list")
    @Operation(summary = "获取用户列表")
    public Result<List<SysUser>> getUserList() {
        return Result.success(userService.getUserList());
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询用户")
    public Result<PageResult<SysUser>> pageUsers(UserQueryDTO queryDTO) {
        return Result.success(userService.pageUsers(queryDTO));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<SysUser> getUserDetail(@PathVariable Long id) {
        return Result.success(userService.getUserDetail(id));
    }
    
    @PostMapping
    @Operation(summary = "创建用户")
    public Result<Long> createUser(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success(userService.createUser(dto));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
    
    @PostMapping("/import")
    @Operation(summary = "批量导入用户")
    public Result<Map<String, Object>> importUsers(@RequestParam("file") MultipartFile file) {
        return Result.success(userService.importUsers(file));
    }
    
    @GetMapping("/template")
    @Operation(summary = "下载用户导入模板")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        byte[] data = ExcelTemplateUtil.generateUserTemplate();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=user_template.xlsx");
        response.getOutputStream().write(data);
    }
    
    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置用户密码")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String newPassword = params.get("newPassword");
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
    
    @PutMapping("/change-password")
    @Operation(summary = "修改当前用户密码")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.changePassword(oldPassword, newPassword);
        return Result.success();
    }
}
