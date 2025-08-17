package pl.kopytka.order.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class ArchitectureTest {

    public static final String BASE_PACKAGE = "pl.kopytka.order";

    @Test
    void checkLayers() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("domain").definedBy("pl.kopytka.order.domain..")
                .layer("application").definedBy("pl.kopytka.order.application..")
                .layer("saga").definedBy("pl.kopytka.order.saga..")
                .whereLayer("domain").mayNotAccessAnyLayer()
                .whereLayer("application").mayOnlyAccessLayers("domain")
                .whereLayer("saga").mayOnlyAccessLayers("domain")
                .check(new ClassFileImporter().importPackages(BASE_PACKAGE));
    }
}