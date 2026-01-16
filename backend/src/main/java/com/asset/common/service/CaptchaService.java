package com.asset.common.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

/**
 * 验证码服务
 */
@Slf4j
@Service
public class CaptchaService {
    
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final String CODE_CHARS = "0123456789";
    
    // 验证码缓存（5分钟过期）
    private final Cache<String, String> captchaCache;
    
    private final Random random = new Random();
    
    public CaptchaService() {
        this.captchaCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(10000)
                .build();
    }
    
    /**
     * 生成验证码
     * @return [验证码ID, Base64图片]
     */
    public String[] generateCaptcha() {
        // 生成验证码文本
        String code = generateCode();
        
        // 生成唯一ID
        String captchaId = UUID.randomUUID().toString();
        
        // 存储验证码（转小写）
        captchaCache.put(captchaId, code.toLowerCase());
        
        // 生成图片
        String base64Image = generateImage(code);
        
        log.info("生成验证码 - ID: {}, Code: {}", captchaId, code);
        
        return new String[]{captchaId, base64Image};
    }
    
    /**
     * 验证验证码
     */
    public boolean verifyCaptcha(String captchaId, String code) {
        if (captchaId == null || code == null) {
            return false;
        }
        
        String cachedCode = captchaCache.getIfPresent(captchaId);
        if (cachedCode == null) {
            log.warn("验证码已过期或不存在 - ID: {}", captchaId);
            return false;
        }
        
        // 验证后立即删除（一次性使用）
        captchaCache.invalidate(captchaId);
        
        boolean result = cachedCode.equalsIgnoreCase(code.trim());
        log.info("验证码校验 - ID: {}, 结果: {}", captchaId, result);
        
        return result;
    }
    
    /**
     * 生成随机验证码文本
     */
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }
    
    /**
     * 生成验证码图片
     */
    private String generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 背景色
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(getRandomColor(180, 220));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        
        // 绘制验证码
        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(getRandomColor(20, 130));
            int x = 20 + i * 25;
            int y = 28 + random.nextInt(5);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
        
        // 绘制干扰点
        for (int i = 0; i < 50; i++) {
            g.setColor(getRandomColor(150, 200));
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.fillOval(x, y, 2, 2);
        }
        
        g.dispose();
        
        // 转换为Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            log.error("生成验证码图片失败", e);
            return "";
        }
    }
    
    /**
     * 获取随机颜色
     */
    private Color getRandomColor(int min, int max) {
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }
}
