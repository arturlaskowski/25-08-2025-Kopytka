package pl.kopytka.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    public static final String BASE_PACKAGE = "pl.kopytka";

    @Test
    void domainShouldNotDependOnWeb() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..web..")
                .check(new ClassFileImporter().importPackages(BASE_PACKAGE));
    }

    @Test
    void domainShouldNotDependOnApplication() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..application..")
                .check(new ClassFileImporter().importPackages(BASE_PACKAGE));
    }
}