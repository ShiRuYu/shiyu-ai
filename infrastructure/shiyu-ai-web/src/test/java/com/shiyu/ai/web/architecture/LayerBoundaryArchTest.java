package com.shiyu.ai.web.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Executable guardrails for the BO/DO and Web boundaries. */
@AnalyzeClasses(packages = "com.shiyu.ai")
class LayerBoundaryArchTest {
    @ArchTest
    static final ArchRule webMustNotDependOnDal = noClasses()
            .that().resideInAnyPackage("com.shiyu.ai.web..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.shiyu.ai.dal..")
            .because("Web is an HTTP adapter and must not access persistence implementations");

    @ArchTest
    static final ArchRule businessMustNotDependOnPersistenceTypes = noClasses()
            .that().resideInAnyPackage(
                    "com.shiyu.ai.agent..", "com.shiyu.ai.auth..", "com.shiyu.ai.education..",
                    "com.shiyu.ai.knowledge..", "com.shiyu.ai.memory..", "com.shiyu.ai.model..",
                    "com.shiyu.ai.record..", "com.shiyu.ai.usage..", "com.shiyu.ai.tool..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.shiyu.ai.dal..dataobject..", "com.shiyu.ai.dal..mapper..")
            .because("DO and Mapper types belong exclusively to DAL");

    @ArchTest
    static final ArchRule repositoryPortsMustNotExposePersistenceTypes = noClasses()
            .that().resideInAnyPackage("com.shiyu.ai..port.repository..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.shiyu.ai.dal..", "com.mybatisflex..")
            .because("Repository ports are business-owned persistence abstractions");
}
