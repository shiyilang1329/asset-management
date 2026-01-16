package com.asset.module.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private List<String> permissions;
    
    public LoginVO(String token, Long userId, String username, String realName) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
    }
}
