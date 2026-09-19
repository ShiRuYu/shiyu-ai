package com.shiyu.ai.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.shiyu.ai",
        importOptions = {
            ImportOption.DoNotIncludeTests.class,
            ArchitectureImportOptions.DoNotIncludeLegacyPlatformBuilds.class
        })
class ContextBoundaryArchitectureTest {
    @ArchTest
    static final ArchRule DOMAIN_AND_APPLICATION_LAYERS_DO_NOT_READ_THREAD_CONTEXT =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "com.shiyu.ai..implementation.application..",
                            "com.shiyu.ai..implementation.domain..")
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName("com.shiyu.ai.common.core.context.UserContextHolder")
                    .because("ActorContext must enter through an explicit application boundary");

    @ArchTest
    static final ArchRule DOMAIN_AND_PORT_LAYERS_DO_NOT_READ_TENANT_SCOPE =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "com.shiyu.ai..implementation.domain..",
                            "com.shiyu.ai..implementation..port..")
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName("com.shiyu.ai.kernel.context.TenantScope")
                    .because(
                            "TenantScope is not a domain or port concern; authorized application"
                                    + " orchestration and persistence adapters validate it");

    @ArchTest
    static final ArchRule LEGACY_KNOWLEDGE_DOMAIN_DOES_NOT_READ_THREAD_CONTEXT =
            noClasses()
                    .that()
                    .resideInAPackage("com.shiyu.ai.knowledge..")
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName("com.shiyu.ai.common.core.context.UserContextHolder")
                    .because("knowledge application services must receive ActorContext explicitly");

    @ArchTest
    static final ArchRule WEB_DOMAIN_ADAPTERS_DO_NOT_READ_THREAD_CONTEXT =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "com.shiyu.ai.web.agent..",
                            "com.shiyu.ai.web.chat..",
                            "com.shiyu.ai.web.common..",
                            "com.shiyu.ai.web.conversation..",
                            "com.shiyu.ai.web.evaluation..",
                            "com.shiyu.ai.web.knowledge..",
                            "com.shiyu.ai.web.memory..",
                            "com.shiyu.ai.web.model..",
                            "com.shiyu.ai.web.openai..",
                            "com.shiyu.ai.web.prompt..",
                            "com.shiyu.ai.web.runtime..",
                            "com.shiyu.ai.web.usage..",
                            "com.shiyu.ai..implementation.web..")
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName("com.shiyu.ai.common.core.context.UserContextHolder")
                    .because(
                            "only the HTTP authentication adapter may translate thread context into"
                                    + " ActorContext");
}
