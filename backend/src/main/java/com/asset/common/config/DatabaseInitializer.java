package com.asset.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            // 检查 sys_user 表是否存在
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sys_user'",
                Integer.class
            );
            
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sys_user'",
                Integer.class
            );
            
            if (count == null || count == 0) {
                log.info("数据库表不存在，开始初始化...");
                initializeDatabase();
            } else {
                log.info("数据库表已存在，跳过初始化");
            }
        } catch (Exception e) {
            log.warn("检查数据库表时出错，尝试初始化: {}", e.getMessage());
            try {
                initializeDatabase();
            } catch (Exception ex) {
                log.error("数据库初始化失败", ex);
            }
        }
    }
    
    private void initializeDatabase() throws Exception {
        log.info("开始执行数据库初始化脚本...");
        
        // 读取 schema.sql
        ClassPathResource schemaResource = new ClassPathResource("db/schema.sql");
        String schemaSql = StreamUtils.copyToString(schemaResource.getInputStream(), StandardCharsets.UTF_8);
        
        // 执行建表语句
        String[] statements = schemaSql.split(";");
        for (String statement : statements) {
            statement = statement.trim();
            if (!statement.isEmpty() && !statement.startsWith("--")) {
                try {
                    jdbcTemplate.execute(statement);
                } catch (Exception e) {
                    log.warn("执行 SQL 语句失败: {}", e.getMessage());
                }
            }
        }
        
        // 读取 data.sql
        try {
            ClassPathResource dataResource = new ClassPathResource("db/data.sql");
            String dataSql = StreamUtils.copyToString(dataResource.getInputStream(), StandardCharsets.UTF_8);
            
            // 执行数据插入语句
            statements = dataSql.split(";");
            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty() && !statement.startsWith("--")) {
                    try {
                        jdbcTemplate.execute(statement);
                    } catch (Exception e) {
                        log.warn("执行数据插入失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("读取 data.sql 失败: {}", e.getMessage());
        }
        
        log.info("数据库初始化完成！");
        log.info("默认管理员账号 - 用户名: admin, 密码: admin123");
    }
}
