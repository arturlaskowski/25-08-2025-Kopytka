package pl.kopytka.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import pl.kopytka.customer.CustomerFacade;

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

    @Test
    void customerModuleShouldNotDependOnOrderModule() {
        noClasses()
                .that()
                .resideInAPackage("pl.kopytka.customer..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("pl.kopytka.order..")
                .check(new ClassFileImporter().importPackages(BASE_PACKAGE));
    }

    @Test
    void orderModuleShouldNotDependOnCustomerModuleExcludeFacade() {
        noClasses()
                .that()
                .resideInAPackage("pl.kopytka.order..")
                .should()
                .dependOnClassesThat(originatesFromLoyaltyModuleAndIsNotFacade())
                .check(new ClassFileImporter().importPackages(BASE_PACKAGE));
    }

    private static DescribedPredicate<JavaClass> originatesFromLoyaltyModuleAndIsNotFacade() {
        return new DescribedPredicate<>("originates from customer module and is not a facade") {
            @Override
            public boolean test(JavaClass input) {
                return input.getPackageName().startsWith("pl.kopytka.customer")
                        && !input.isAssignableFrom(CustomerFacade.class);
            }
        };
    }
}