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
 * 定义 Open API 基础设施或应用能力的配置项及装配规则。
 */
@AutoConfigureBefore(SpringDocConfiguration.class)
@AutoConfiguration
public class OpenApiConfig {

    /**
     * TOKEN_HEADER 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final String TOKEN_HEADER = "Authorization";

    /**
     * 创建或保存 Open API 相关业务数据，并返回处理结果。
     *
     * @return 返回 Open API 相关操作生成的结果数据。
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
