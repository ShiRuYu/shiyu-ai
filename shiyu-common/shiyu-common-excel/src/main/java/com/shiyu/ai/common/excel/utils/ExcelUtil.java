package com.shiyu.ai.common.excel.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.IdUtil;
import com.shiyu.ai.common.core.CharConstants;
import com.shiyu.ai.common.core.utils.file.FileUtils;
import com.shiyu.ai.common.excel.convert.ExcelBigNumberConvert;
import com.shiyu.ai.common.excel.core.CellMergeStrategy;
import com.shiyu.ai.common.excel.core.DefaultExcelListener;
import com.shiyu.ai.common.excel.core.ExcelListener;
import com.shiyu.ai.common.excel.core.ExcelResult;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.write.builder.ExcelWriterSheetBuilder;
import org.apache.fesod.sheet.write.metadata.WriteSheet;
import org.apache.fesod.sheet.write.metadata.fill.FillConfig;
import org.apache.fesod.sheet.write.metadata.fill.FillWrapper;
import org.apache.fesod.sheet.write.style.column.LongestMatchColumnWidthStyleStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Excel相关处理
 */
public class ExcelUtil {

    // ==============================
    //  导入
    // ==============================

    /**
     * 同步导入(适用于小数据量)
     */
    public static <T> List<T> importExcel(InputStream is, Class<T> clazz) {
        return FesodSheet.read(is).head(clazz).autoCloseStream(false).sheet().doReadSync();
    }

    /**
     * 使用校验监听器 异步导入 同步返回
     *
     * @param isValidate 是否 Validator 检验
     */
    public static <T> ExcelResult<T> importExcel(InputStream is, Class<T> clazz, boolean isValidate) {
        DefaultExcelListener<T> listener = new DefaultExcelListener<>(isValidate);
        FesodSheet.read(is, clazz, listener).sheet().doRead();
        return listener.getExcelResult();
    }

    /**
     * 使用自定义监听器 异步导入 自定义返回
     */
    public static <T> ExcelResult<T> importExcel(InputStream is, Class<T> clazz, ExcelListener<T> listener) {
        FesodSheet.read(is, clazz, listener).sheet().doRead();
        return listener.getExcelResult();
    }

    // ==============================
    //  导出
    // ==============================

    /**
     * 导出excel（输出到流）
     *
     * @param list      导出数据集合
     * @param sheetName 工作表的名称
     * @param clazz     实体类
     * @param merge     是否合并单元格
     * @param os        输出流
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, boolean merge, OutputStream os) {
        ExcelWriterSheetBuilder builder = FesodSheet.write(os, clazz)
            .autoCloseStream(false)
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .registerConverter(new ExcelBigNumberConvert())
            .sheet(sheetName);
        if (merge) {
            builder.registerWriteHandler(new CellMergeStrategy(list, true));
        }
        builder.doWrite(list);
    }

    /**
     * 导出excel（输出到HTTP响应）
     *
     * @param list      导出数据集合
     * @param sheetName 工作表的名称 / 文件名
     * @param clazz     实体类
     * @param merge     是否合并单元格
     * @param response  HTTP响应
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, boolean merge, HttpServletResponse response) {
        writeToResponse(sheetName, response, os ->
            exportExcel(list, sheetName, clazz, merge, os));
    }

    // ==============================
    //  模板导出
    // ==============================

    /**
     * 单表多数据模板导出 模板格式为 {.属性}（输出到流）
     *
     * @param data         模板需要的数据
     * @param templatePath 模板路径（classpath 下的路径，如 excel/temp.xlsx）
     * @param os           输出流
     */
    public static void exportTemplate(List<Object> data, String templatePath, OutputStream os) {
        if (CollUtil.isEmpty(data)) {
            throw new IllegalArgumentException("数据为空");
        }
        TemplateContext ctx = buildTemplateContext(os, templatePath);
        for (Object d : data) {
            ctx.writer().fill(d, ctx.sheet());
        }
        ctx.writer().finish();
    }

    /**
     * 单表多数据模板导出 模板格式为 {.属性}（输出到HTTP响应）
     *
     * @param data         模板需要的数据
     * @param filename     文件名（不含扩展名）
     * @param templatePath 模板路径（classpath 下的路径，如 excel/temp.xlsx）
     * @param response     HTTP响应
     */
    public static void exportTemplate(List<Object> data, String filename, String templatePath, HttpServletResponse response) {
        writeToResponse(filename, response, os ->
            exportTemplate(data, templatePath, os));
    }

    /**
     * 多表多数据模板导出 模板格式为 {key.属性}（输出到流）
     *
     * @param data         模板需要的数据（key 对应模板中的 {key.属性} 前缀）
     * @param templatePath 模板路径（classpath 下的路径，如 excel/temp.xlsx）
     * @param os           输出流
     */
    public static void exportTemplateMultiList(Map<String, Object> data, String templatePath, OutputStream os) {
        if (CollUtil.isEmpty(data)) {
            throw new IllegalArgumentException("数据为空");
        }
        TemplateContext ctx = buildTemplateContext(os, templatePath);
        FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof Collection<?> collection) {
                ctx.writer().fill(new FillWrapper(entry.getKey(), collection), fillConfig, ctx.sheet());
            } else {
                ctx.writer().fill(entry.getValue(), ctx.sheet());
            }
        }
        ctx.writer().finish();
    }

    /**
     * 多表多数据模板导出 模板格式为 {key.属性}（输出到HTTP响应）
     *
     * @param data         模板需要的数据
     * @param filename     文件名（不含扩展名）
     * @param templatePath 模板路径（classpath 下的路径，如 excel/temp.xlsx）
     * @param response     HTTP响应
     */
    public static void exportTemplateMultiList(Map<String, Object> data, String filename, String templatePath, HttpServletResponse response) {
        writeToResponse(filename, response, os ->
            exportTemplateMultiList(data, templatePath, os));
    }

    // ==============================
    //  表达式转换
    // ==============================

    /**
     * 解析导出值 0=男,1=女,2=未知
     *
     * @param propertyValue 参数值
     * @param converterExp  翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    public static String convertByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(CharConstants.COMMA);
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(propertyValue, separator)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[0].equals(value)) {
                        propertyString.append(itemArray[1]).append(separator);
                        break;
                    }
                }
            } else {
                if (itemArray[0].equals(propertyValue)) {
                    return itemArray[1];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 反向解析值 男=0,女=1,未知=2
     *
     * @param propertyValue 参数值
     * @param converterExp  翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    public static String reverseByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(CharConstants.COMMA);
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(propertyValue, separator)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[1].equals(value)) {
                        propertyString.append(itemArray[0]).append(separator);
                        break;
                    }
                }
            } else {
                if (itemArray[1].equals(propertyValue)) {
                    return itemArray[0];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 编码文件名
     */
    public static String encodingFilename(String filename) {
        return IdUtil.fastSimpleUUID() + "_" + filename + ".xlsx";
    }

    // ==============================
    //  私有辅助
    // ==============================

    /**
     * 构建模板写入上下文（消除 FesodSheet 重复构建代码）
     */
    private static TemplateContext buildTemplateContext(OutputStream os, String templatePath) {
        ClassPathResource templateResource = new ClassPathResource(templatePath);
        ExcelWriter writer = FesodSheet.write(os)
            .withTemplate(templateResource.getStream())
            .autoCloseStream(false)
            .registerConverter(new ExcelBigNumberConvert())
            .build();
        return new TemplateContext(writer, FesodSheet.writerSheet().build());
    }

    /**
     * 写入HTTP响应（消除重复的 resetResponse + getOutputStream + try/catch）
     */
    private static void writeToResponse(String filename, HttpServletResponse response,
                                         IoConsumer consumer) {
        try {
            resetResponse(filename, response);
            ServletOutputStream os = response.getOutputStream();
            consumer.accept(os);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常", e);
        }
    }

    /**
     * 重置响应体
     */
    private static void resetResponse(String sheetName, HttpServletResponse response) {
        String filename = encodingFilename(sheetName);
        FileUtils.setAttachmentResponseHeader(response, filename);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
    }

    @FunctionalInterface
    private interface IoConsumer {
        void accept(OutputStream os) throws IOException;
    }

    /**
     * 模板写入上下文，承载 writer + sheet 避免重复传递
     */
    private record TemplateContext(ExcelWriter writer, WriteSheet sheet) {}

}
