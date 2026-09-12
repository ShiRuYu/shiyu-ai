package com.shiyu.ai.web.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;

/**
 * {@code OpenApiConfig} 提供Web模块的配置项，并集中声明其默认值和运行约束。
 */
@AutoConfigureBefore(SpringDocConfiguration.class)
@AutoConfiguration
public class OpenApiConfig {

    /**
     * TOKEN_HEADER 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final String TOKEN_HEADER = "Authorization";

    /**
     * {@code openApi} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("时雨AI")
                                .description("接口文档")
                                .contact(new Contact().name("作者").email("邮箱").url("博客地址或不填"))
                                // 参考 Apache 2.0 许可及地址，你可以不配此项
                                .license(
                                        new License()
                                                .name("Apache 2.0")
                                                .url(
                                                        "https://www.apache.org/licenses/LICENSE-2.0.html"))
                                .version("0.1"))
                // 引入外部的文档
                .externalDocs(
                        new ExternalDocumentation()
                                .description("SpringDoc Full Documentation")
                                .url("https://springdoc.org/"));
    }
}
