package pl.kopytka.common;

import org.assertj.core.api.AbstractStringAssert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Utility class for testing exception messages consistently across all tests
 */
public class ExceptionTestUtils {

    /**
     * Asserts that the response body contains the expected exception message
     * @param responseBody the response body to check
     * @param expectedMessage the expected message
     * @return AbstractStringAssert for further assertions
     */
    public static AbstractStringAssert<?> assertExceptionMessage(Object responseBody, String expectedMessage) {
        return assertThat(responseBody)
                .isNotNull()
                .extracting("message")
                .asString()
                .contains(expectedMessage);
    }

    /**
     * Asserts that the response body contains the exact exception message
     * @param responseBody the response body to check
     * @param expectedMessage the expected exact message
     * @return AbstractStringAssert for further assertions
     */
    public static AbstractStringAssert<?> assertExactExceptionMessage(Object responseBody, String expectedMessage) {
        return assertThat(responseBody)
                .isNotNull()
                .extracting("message")
                .asString()
                .isEqualTo(expectedMessage);
    }
}
