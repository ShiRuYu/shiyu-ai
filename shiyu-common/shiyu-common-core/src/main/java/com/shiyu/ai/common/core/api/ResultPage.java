package com.shiyu.ai.common.core.api;

import cn.hutool.http.HttpStatus;
import com.shiyu.ai.common.core.enums.BizResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 */
@Data
@NoArgsConstructor
public class ResultPage<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 列表数据
     */
    private List<T> data;

    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 消息内容
     */
    private Boolean success = true;

    /**
     * 分页
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public ResultPage(List<T> list, long total) {
        this.data = list;
        this.total = total;
        this.code = BizResultCode.SUC.getCode();
        this.msg = "查询成功";
    }
}
