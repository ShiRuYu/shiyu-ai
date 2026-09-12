package com.shiyu.ai.education.implementation.web.path;

import com.shiyu.ai.common.web.config.WebPublicPathContributor;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 向 Web 安全配置贡献教育模块的公开访问路径。
 */
@Component
public class EducationWebPublicPathContributor implements WebPublicPathContributor {

    /**
     * {@code publicPathPatterns} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Collection<String> publicPathPatterns() {
        return List.of("/api/education/education-resources/**");
    }
}
