package com.shiyu.ai.kernel.page;

/** One-based bounded paging request shared by contracts. */
public record PageRequest(int pageNumber, int pageSize) {

    public static final int MAX_PAGE_SIZE = 200;

    public PageRequest {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be at least 1");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    public long offset() {
        return Math.multiplyExact((long) pageNumber - 1, pageSize);
    }
}
