package org.hwp;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

/**
 * somewhat "clean" architecture:
 * domain in the center, interfaces at the outside
 * dependency-rule -> no dependencies from domain to the interfaces
 */
class StructureTest {

    private static class ReallyNoTestStuff implements ImportOption {

        // vscode-java build classpath: ./bin/..
        // otherwise run via vscode-java-test-runner does include tests..
        private static final Pattern BIN_TEST = Pattern.compile(".*/bin/test/.*");

        @Override
        public boolean includes(Location location) {
            return ImportOption.Predefined.DO_NOT_INCLUDE_TESTS.includes(location) && !location.matches(BIN_TEST);
        }

    }

    private static final String BASE = "org.hwp";
    private static final String DOMAIN = BASE + ".domain..";
    private static final String APPLICATION_SERVICE = BASE + ".application..";
    private static final String INTERFACES = BASE + ".interfaces..";

    private static final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(new ReallyNoTestStuff()).importPackages(BASE);

    @Test
    void test_no_classes_outside_defined_layers() {
        ArchRule myRule = ArchRuleDefinition.classes().should().resideInAnyPackage(DOMAIN, APPLICATION_SERVICE,
                INTERFACES);

        myRule.check(importedClasses);
    }

    @Test
    void test_onion_like() {

        ArchRule myRule = Architectures.onionArchitecture().withOptionalLayers(true).domainModels(DOMAIN)
                .applicationServices(APPLICATION_SERVICE)
                .adapter("outside world", INTERFACES);

        myRule.check(importedClasses);
    }

    @Test
    void test_no_controller_outside_of_interfaces() {

        ArchRule myRule = ArchRuleDefinition.noClasses().that().resideOutsideOfPackage(INTERFACES).should()
                .haveSimpleNameContaining("Controller");

        myRule.check(importedClasses);
    }

    @Test
    void test_cycle_free_domain_slices() {
        ArchRule myRule = SlicesRuleDefinition.slices().matching(BASE + ".domain.(*)..").should().beFreeOfCycles();
        myRule.check(importedClasses);
    }
}
