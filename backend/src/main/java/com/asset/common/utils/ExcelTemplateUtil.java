package com.asset.common.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ExcelTemplateUtil {
    
    /**
     * 生成用户导入模板
     */
    public static byte[] generateUserTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户数据");
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"用户名", "密码", "真实姓名", "邮箱", "电话"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }
            
            // 创建示例数据行
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("zhangsan");
            row1.createCell(1).setCellValue("123456");
            row1.createCell(2).setCellValue("张三");
            row1.createCell(3).setCellValue("zhangsan@company.com");
            row1.createCell(4).setCellValue("13900139001");
            
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("lisi");
            row2.createCell(1).setCellValue("123456");
            row2.createCell(2).setCellValue("李四");
            row2.createCell(3).setCellValue("lisi@company.com");
            row2.createCell(4).setCellValue("13900139002");
            
            // 输出到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 生成资产导入模板
     */
    public static byte[] generateAssetTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("资产数据");
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"资产编号", "资产名称", "分类ID", "品牌", "型号", "采购价格", "采购日期", "供应商", "存放位置"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }
            
            // 创建示例数据行
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("PC-2024-003");
            row1.createCell(1).setCellValue("华为笔记本");
            row1.createCell(2).setCellValue("2");
            row1.createCell(3).setCellValue("华为");
            row1.createCell(4).setCellValue("MateBook 14");
            row1.createCell(5).setCellValue("6999");
            row1.createCell(6).setCellValue("2024-01-15");
            row1.createCell(7).setCellValue("华为官方");
            row1.createCell(8).setCellValue("研发部-102室");
            
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("DESK-2024-002");
            row2.createCell(1).setCellValue("办公椅");
            row2.createCell(2).setCellValue("6");
            row2.createCell(3).setCellValue("震旦");
            row2.createCell(4).setCellValue("SD-800");
            row2.createCell(5).setCellValue("800");
            row2.createCell(6).setCellValue("2024-01-10");
            row2.createCell(7).setCellValue("震旦办公家具");
            row2.createCell(8).setCellValue("市场部-402室");
            
            // 输出到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 生成员工导入模板
     */
    public static byte[] generateEmployeeTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("员工数据");
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"工号", "姓名", "手机号", "邮箱", "部门", "职位"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }
            
            // 创建示例数据行
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("E001");
            row1.createCell(1).setCellValue("张三");
            row1.createCell(2).setCellValue("13900139001");
            row1.createCell(3).setCellValue("zhangsan@company.com");
            row1.createCell(4).setCellValue("研发部");
            row1.createCell(5).setCellValue("工程师");
            
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("E002");
            row2.createCell(1).setCellValue("李四");
            row2.createCell(2).setCellValue("13900139002");
            row2.createCell(3).setCellValue("lisi@company.com");
            row2.createCell(4).setCellValue("市场部");
            row2.createCell(5).setCellValue("经理");
            
            // 输出到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}