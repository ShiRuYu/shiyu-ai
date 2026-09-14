package com.shiyu.ai.kernel.page;

/**
 * 分页查询page请求。
 * @param pageNumber 页码，表示该记录组件承载的数据。
 * @param pageSize 页大小，表示该记录组件承载的数据。
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
     * {@code offset} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public long offset() {
        return Math.multiplyExact((long) pageNumber - 1, pageSize);
    }
}
