package pl.kopytka.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Base class for integration tests.
 * Provides common infrastructure for HTTP testing in acceptance tests.
 */
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate testRestTemplate;
    
    /**
     * Get the base URL for the running service.
     * @param basePath The base path for the API, e.g., "/api/customers"
     * @return The full URL including host, port and base path
     */
    protected String getBaseUrl(String basePath) {
        return "http://localhost:" + port + basePath;
    }
}
