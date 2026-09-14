package com.shiyu.ai.education.implementation.database;

import com.shiyu.ai.common.core.database.DatabaseBaselineContributor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 向数据库初始化流程贡献教育模块的 schema 和种子资源。
 */
@Component
public final class EducationDatabaseBaselineContributor
        implements DatabaseBaselineContributor {

    private static final Set<String> TABLES =
            Set.of(
                    "EDU_ABILITY",
                    "EDU_ACHIEVEMENT",
                    "EDU_CHAPTER",
                    "EDU_COURSE",
                    "EDU_COURSE_CHAPTER",
                    "EDU_COURSE_KNOWLEDGE",
                    "EDU_COURSE_SECTION",
                    "EDU_EXAM",
                    "EDU_EXAM_QUESTION",
                    "EDU_EXAM_SECTION",
                    "EDU_KNOWLEDGE_TEXTBOOK",
                    "EDU_LEARNING_STATE",
                    "EDU_QUESTION",
                    "EDU_QUESTION_KNOWLEDGE",
                    "EDU_RESOURCE",
                    "EDU_RESOURCE_KNOWLEDGE",
                    "EDU_REVIEW_TASK",
                    "EDU_STUDENT",
                    "EDU_STUDY_PLAN",
                    "EDU_STUDY_PLAN_ITEM",
                    "EDU_STUDY_RECORD",
                    "EDU_SUBJECT",
                    "EDU_TEACHER",
                    "EDU_TEXTBOOK",
                    "EDU_WRONG_QUESTION");

    @Override
    public int order() {
        return 100;
    }

    @Override
    public List<String> schemaResources() {
        return List.of("classpath:db/baseline/h2/schema/education/07_education.sql");
    }

    @Override
    public List<String> seedResources() {
        return List.of(
                "classpath:db/baseline/h2/seed/education/01_auth.sql",
                "classpath:db/baseline/h2/seed/education/02_navigation.sql",
                "classpath:db/baseline/h2/seed/education/07_education.sql",
                "classpath:db/baseline/h2/seed/education/08_learning_progress.sql",
                "classpath:db/baseline/h2/seed/education/09_curriculum.sql",
                "classpath:db/baseline/h2/seed/education/10_resources.sql",
                "classpath:db/baseline/h2/seed/education/11_resource_variants.sql");
    }

    @Override
    public Set<String> expectedTables() {
        return TABLES;
    }
}
