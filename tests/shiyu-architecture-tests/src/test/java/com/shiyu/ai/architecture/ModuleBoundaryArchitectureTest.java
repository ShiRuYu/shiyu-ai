package com.shiyu.ai.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.junit.jupiter.api.Test;

@AnalyzeClasses(
        packages = "com.shiyu.ai",
        importOptions = {
            ImportOption.DoNotIncludeTests.class,
            ArchitectureImportOptions.DoNotIncludeLegacyPlatformBuilds.class
        })
class ModuleBoundaryArchitectureTest {
    @ArchTest
    static final ArchRule IMPLEMENTATIONS_ARE_NOT_COUPLED =
            slices().matching("com.shiyu.ai.(*)..implementation..")
                    .should()
                    .notDependOnEachOther()
                    .because(
                            "bounded contexts may depend on another context only through its"
                                    + " contract");

    @ArchTest
    static final ArchRule CONTRACTS_ARE_FRAMEWORK_FREE =
            noClasses()
                    .that()
                    .resideInAPackage("com.shiyu.ai..contract..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "com.baomidou..",
                            "org.apache.ibatis..",
                            "org.bsc.langgraph4j..")
                    .because("contracts must remain portable and framework free");

    @Test
    void contractAndImplementationPackagesArePresent() {
        JavaClasses importedClasses =
                new ClassFileImporter()
                        .withImportOption(new ImportOption.DoNotIncludeTests())
                        .withImportOption(
                                new ArchitectureImportOptions.DoNotIncludeLegacyPlatformBuilds())
                        .importPackages("com.shiyu.ai");

        assertTrue(
                importedClasses.stream()
                        .anyMatch(javaClass -> javaClass.getPackageName().contains(".contract")),
                "contract packages must be present in the architecture scan");
        assertTrue(
                importedClasses.stream()
                        .anyMatch(
                                javaClass ->
                                        javaClass.getPackageName().contains(".implementation")),
                "implementation packages must be present in the architecture scan");
    }
}
