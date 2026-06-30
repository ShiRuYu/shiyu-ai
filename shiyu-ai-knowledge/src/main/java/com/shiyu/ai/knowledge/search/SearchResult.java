package com.shiyu.ai.knowledge.search;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 搜索结果 DTO
 */
@Data
@AllArgsConstructor
public class SearchResult {
    private Long id;
    private String name;
    private String code;
    private String category;
    private float score;
}
