package com.asset.common.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录尝试限制服务
 * 防止暴力破解和恶意登录
 */
@Service
public class LoginAttemptService {
    
    // 最大失败次数
    private static final int MAX_ATTEMPTS = 5;
    
    // 锁定时间（分钟）
    private static final int LOCK_TIME_MINUTES = 15;
    
    // 失败次数缓存（用户名 -> 失败次数）
    private final Cache<String, AtomicInteger> attemptsCache;
    
    // 锁定缓存（用户名 -> 锁定时间戳）
    private final Cache<String, Long> lockCache;
    
    public LoginAttemptService() {
        // 失败次数缓存，30分钟后自动清除
        this.attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(30))
                .maximumSize(1000)
                .build();
        
        // 锁定缓存，锁定时间后自动清除
        this.lockCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(LOCK_TIME_MINUTES))
                .maximumSize(1000)
                .build();
    }
    
    /**
     * 登录成功，清除失败记录
     */
    public void loginSucceeded(String username) {
        attemptsCache.invalidate(username);
        lockCache.invalidate(username);
    }
    
    /**
     * 登录失败，记录失败次数
     */
    public void loginFailed(String username) {
        AtomicInteger attempts = attemptsCache.get(username, k -> new AtomicInteger(0));
        int failCount = attempts.incrementAndGet();
        
        // 如果失败次数达到最大值，锁定账号
        if (failCount >= MAX_ATTEMPTS) {
            lockCache.put(username, System.currentTimeMillis());
        }
    }
    
    /**
     * 检查账号是否被锁定
     */
    public boolean isBlocked(String username) {
        return lockCache.getIfPresent(username) != null;
    }
    
    /**
     * 获取剩余尝试次数
     */
    public int getRemainingAttempts(String username) {
        AtomicInteger attempts = attemptsCache.getIfPresent(username);
        if (attempts == null) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - attempts.get());
    }
    
    /**
     * 获取锁定剩余时间（分钟）
     */
    public long getLockRemainingMinutes(String username) {
        Long lockTime = lockCache.getIfPresent(username);
        if (lockTime == null) {
            return 0;
        }
        long elapsedMinutes = (System.currentTimeMillis() - lockTime) / 60000;
        return Math.max(0, LOCK_TIME_MINUTES - elapsedMinutes);
    }
    
    /**
     * 手动解锁账号（管理员操作）
     */
    public void unlock(String username) {
        attemptsCache.invalidate(username);
        lockCache.invalidate(username);
    }
}
