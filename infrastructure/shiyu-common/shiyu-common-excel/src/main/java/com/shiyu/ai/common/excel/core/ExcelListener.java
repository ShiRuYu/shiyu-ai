package com.shiyu.ai.common.excel.core;


import org.apache.fesod.sheet.read.listener.ReadListener;

/**
 * Excel 导入监听
 */
public interface ExcelListener<T> extends ReadListener<T> {

    ExcelResult<T> getExcelResult();

}
