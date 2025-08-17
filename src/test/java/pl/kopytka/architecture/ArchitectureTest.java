package pl.kopytka.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class ArchitectureTest {

    public static final String BASE_PACKAGE = "pl.kopytka";
    private final JavaClasses classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    @Test
    void modulesShouldBeIndependent() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("customer").definedBy("pl.kopytka.customer..")
                .layer("order").definedBy("pl.kopytka.order..")
                .layer("track-order").definedBy("pl.kopytka.trackorder..")
                .whereLayer("customer").mayNotAccessAnyLayer()
                .whereLayer("order").mayNotAccessAnyLayer()
                .whereLayer("track-order").mayNotAccessAnyLayer()
                .check(classes);
    }

}