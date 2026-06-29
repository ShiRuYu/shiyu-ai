package com.shiyu.ai.knowledge.dto;

import com.shiyu.ai.common.core.api.PageQuery;

public class KnowledgePageQuery extends PageQuery {

    private String subjectCode;

    private Integer grade;

    private String keyword;

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
