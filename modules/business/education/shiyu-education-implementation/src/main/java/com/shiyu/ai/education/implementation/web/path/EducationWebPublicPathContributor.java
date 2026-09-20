package com.shiyu.ai.education.implementation.web.path;

import com.shiyu.ai.common.web.config.WebPublicPathContributor;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 向 教育 Web Public Path 所属的应用或基础设施注册必要的扩展能力。
 */
@Component
public class EducationWebPublicPathContributor implements WebPublicPathContributor {

    /**
     * 执行 教育 Web Public Path 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public Collection<String> publicPathPatterns() {
        return List.of("/api/education/education-resources/**");
    }
}
