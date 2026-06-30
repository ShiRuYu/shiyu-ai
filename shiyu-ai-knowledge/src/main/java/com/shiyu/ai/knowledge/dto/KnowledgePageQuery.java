package com.shiyu.ai.knowledge.dto;

import com.shiyu.ai.common.core.api.PageQuery;

public class KnowledgePageQuery extends PageQuery {

    private String category;

    private String keyword;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
