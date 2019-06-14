package de.uni_mannheim.informatik.dws.ontmatching.validation;

import java.io.File;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class OntologyValidationServiceTest {

    public static Stream<Arguments> provideOntologyValidationService() {
        return Stream.of(
                Arguments.of(new JenaOntologyValidationService()),
                Arguments.of(new OwlApiOntologyValidationService())
        );
    }

    @ParameterizedTest
    @MethodSource("provideOntologyValidationService")
    public void test(OntologyValidationService ovs) {
        ovs.loadOntology(new File("src/test/resources/cmt.owl"));

        assertTrue(ovs.isOntParseable());
        assertTrue(ovs.isOntologyDefined());
        assertTrue(ovs.getNumberOfClasses() > 0);
        assertTrue(ovs.getNumberOfInstances() == 0);
        assertTrue(ovs.getNumberOfStatements() > 0);

        System.out.println(ovs.toString());

    }

}
