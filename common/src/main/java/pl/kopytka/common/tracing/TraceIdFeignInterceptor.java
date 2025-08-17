package pl.kopytka.common.tracing;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Feign interceptor that automatically propagates the trace ID from MDC
 * to outgoing HTTP requests.
 */
@Slf4j
public class TraceIdFeignInterceptor implements RequestInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    public void apply(RequestTemplate template) {
        String traceId = MDC.get(TRACE_ID_MDC_KEY);

        if (traceId != null && !traceId.trim().isEmpty()) {
            template.header(TRACE_ID_HEADER, traceId);
        }
    }
}