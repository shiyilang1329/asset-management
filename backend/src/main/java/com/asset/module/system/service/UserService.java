package com.asset.module.system.service;

import com.asset.common.exception.BusinessException;
import com.asset.common.result.PageResult;
import com.asset.common.utils.ExcelUtil;
import com.asset.module.system.dto.UserCreateDTO;
import com.asset.module.system.dto.UserQueryDTO;
import com.asset.module.system.dto.UserUpdateDTO;
import com.asset.module.system.entity.SysUser;
import com.asset.module.system.mapper.SysUserMapper;
import com.asset.security.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public List<SysUser> getUserList() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStatus, 1); // 只返回启用的用户
        wrapper.orderByAsc(SysUser::getUsername);
        return userMapper.selectList(wrapper);
    }
    
    public PageResult<SysUser> pageUsers(UserQueryDTO queryDTO) {
        Page<SysUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(SysUser::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getRealName())) {
            wrapper.like(SysUser::getRealName, queryDTO.getRealName());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }
        
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        
        // 清除密码字段
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return new PageResult<>(result.getRecords(), result.getTotal(), 
                                queryDTO.getPageNum(), queryDTO.getPageSize());
    }
    
    public Long createUser(UserCreateDTO dto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1); // 默认启用
        user.setCreateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        return user.getId();
    }
    
    public void updateUser(Long id, UserUpdateDTO dto) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        BeanUtils.copyProperties(dto, user);
        userMapper.updateById(user);
    }
    
    public void deleteUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 软删除：设置状态为禁用
        user.setStatus(0);
        userMapper.updateById(user);
    }
    
    public SysUser getUserDetail(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null); // 不返回密码
        return user;
    }
    
    public Map<String, Object> importUsers(MultipartFile file) {
        try {
            List<List<String>> rows = ExcelUtil.readExcel(file);
            
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMsg = new StringBuilder();
            
            for (int i = 0; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                if (row.size() < 3) {
                    failCount++;
                    errorMsg.append("第").append(i + 2).append("行数据不完整; ");
                    continue;
                }
                
                try {
                    String username = row.get(0);
                    String password = row.get(1);
                    String realName = row.get(2);
                    String email = row.size() > 3 ? row.get(3) : "";
                    String phone = row.size() > 4 ? row.get(4) : "";
                    
                    // 检查用户名是否已存在
                    LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(SysUser::getUsername, username);
                    if (userMapper.selectCount(wrapper) > 0) {
                        failCount++;
                        errorMsg.append("第").append(i + 2).append("行用户名已存在; ");
                        continue;
                    }
                    
                    // 验证密码长度
                    if (password.length() < 6) {
                        failCount++;
                        errorMsg.append("第").append(i + 2).append("行密码少于6位; ");
                        continue;
                    }
                    
                    // 创建用户
                    SysUser user = new SysUser();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setRealName(realName);
                    user.setEmail(email);
                    user.setPhone(phone);
                    user.setStatus(1);
                    user.setCreateTime(LocalDateTime.now());
                    
                    userMapper.insert(user);
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
    
    /**
     * 管理员重置用户密码
     */
    public void resetPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("密码至少6位");
        }
        
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
    
    /**
     * 用户修改自己的密码
     */
    public void changePassword(String oldPassword, String newPassword) {
        // 获取当前登录用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        
        Long userId = Long.parseLong(authentication.getName());
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 验证新密码
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码至少6位");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
