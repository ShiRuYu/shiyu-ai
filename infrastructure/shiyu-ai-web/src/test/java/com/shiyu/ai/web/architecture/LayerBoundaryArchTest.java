package com.shiyu.ai.web.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Executable guardrails for the BO/DO and Web boundaries. */
@AnalyzeClasses(packages = "com.shiyu.ai")
@Tag("dev")
class LayerBoundaryArchTest {
    @Test
    void layerRulesAreActuallyExecutedByTheMavenGate() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.shiyu.ai");
        webMustNotDependOnDal.check(classes);
        businessMustNotDependOnPersistenceTypes.check(classes);
        repositoryPortsMustNotExposePersistenceTypes.check(classes);
        domainApplicationMustNotReadUserContextHolder.check(classes);
        domainApplicationMustNotReadTenantScope.check(classes);
    }

    @ArchTest
    static final ArchRule webMustNotDependOnDal = noClasses()
            .that().resideInAnyPackage("com.shiyu.ai.web..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.shiyu.ai.common.mybatis..")
            .because("Web is an HTTP adapter and must not access persistence implementations");

    @ArchTest
    static final ArchRule businessMustNotDependOnPersistenceTypes = noClasses()
            .that().resideInAnyPackage(
                    "com.shiyu.ai.agent..", "com.shiyu.ai.auth..", "com.shiyu.ai.education..",
                    "com.shiyu.ai.knowledge..", "com.shiyu.ai.memory..", "com.shiyu.ai.model..",
                    "com.shiyu.ai.record..", "com.shiyu.ai.governance..", "com.shiyu.ai.tool..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.shiyu.ai.common.mybatis..dataobject..")
            .because("DO and Mapper types belong exclusively to their domain implementation or technical support");

    @ArchTest
    static final ArchRule repositoryPortsMustNotExposePersistenceTypes = noClasses()
            .that().resideInAnyPackage("com.shiyu.ai..port.repository..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.shiyu.ai.common.mybatis..", "com.mybatisflex..")
            .because("Repository ports are business-owned persistence abstractions");

    @ArchTest
    static final ArchRule domainApplicationMustNotReadUserContextHolder = noClasses()
            .that().resideInAnyPackage(
                    "com.shiyu.ai.agent..", "com.shiyu.ai.auth..", "com.shiyu.ai.conversation..",
                    "com.shiyu.ai.education..", "com.shiyu.ai.knowledge..", "com.shiyu.ai.memory..",
                    "com.shiyu.ai.model..", "com.shiyu.ai.record..", "com.shiyu.ai.governance..",
                    "com.shiyu.ai.tooling..")
            .and().resideOutsideOfPackages("..web..")
            .should().dependOnClassesThat()
            .haveFullyQualifiedName("com.shiyu.ai.common.core.domain.UserContextHolder")
            .because("Domain/application code must receive ActorContext explicitly; thread context is an HTTP adapter concern");

    @ArchTest
    static final ArchRule domainApplicationMustNotReadTenantScope = noClasses()
            .that().resideInAnyPackage(
                    "com.shiyu.ai.agent..", "com.shiyu.ai.auth..", "com.shiyu.ai.conversation..",
                    "com.shiyu.ai.education..", "com.shiyu.ai.knowledge..", "com.shiyu.ai.memory..",
                    "com.shiyu.ai.model..", "com.shiyu.ai.record..", "com.shiyu.ai.governance..",
                    "com.shiyu.ai.tooling..")
            .and().resideOutsideOfPackages("..web..")
            .should().dependOnClassesThat()
            .haveFullyQualifiedName("com.shiyu.ai.kernel.context.TenantScope")
            .because("Domain/application code must receive ActorContext explicitly; thread context is an HTTP adapter concern");
}
