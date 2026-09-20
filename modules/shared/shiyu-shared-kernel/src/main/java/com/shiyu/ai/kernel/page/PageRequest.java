package com.shiyu.ai.kernel.page;

/**
 * 封装 Page 相关的不可变数据及其字段约束。
 */
public record PageRequest(int pageNumber, int pageSize) {

    /**
     * 大小，表示当前对象中的对应属性。
     */
    public static final int MAX_PAGE_SIZE = 200;

    public PageRequest {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be at least 1");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    /**
     * 执行 Page 相关业务数据，并返回处理结果。
     *
     * @return 返回 Page 相关操作生成的结果数据。
     */
    public long offset() {
        return Math.multiplyExact((long) pageNumber - 1, pageSize);
    }
}
