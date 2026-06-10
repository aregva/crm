package com.gym.crm.rest.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionLoggingFilter extends OncePerRequestFilter {
    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingFilter.class);
    private static final int MAX_LOGGED_BODY_LENGTH = 2_000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String transactionId = resolveTransactionId(request);
        MDC.put("transactionId", transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            log.info("REST request started method={}, endpoint={}, query={}",
                    request.getMethod(), request.getRequestURI(), request.getQueryString());
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            log.info("REST request completed method={}, endpoint={}, request={}, status={}, response={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    body(wrappedRequest.getContentAsByteArray(), wrappedRequest.getCharacterEncoding()),
                    wrappedResponse.getStatus(),
                    body(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding()));
        } catch (ServletException | IOException | RuntimeException ex) {
            log.error("REST request failed method={}, endpoint={}, request={}, status={}, error={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    body(wrappedRequest.getContentAsByteArray(), wrappedRequest.getCharacterEncoding()),
                    wrappedResponse.getStatus(),
                    ex.getMessage());
            throw ex;
        } finally {
            wrappedResponse.copyBodyToResponse();
            MDC.remove("transactionId");
        }
    }

    private String resolveTransactionId(HttpServletRequest request) {
        String incoming = request.getHeader(TRANSACTION_ID_HEADER);
        if (incoming == null || incoming.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return incoming;
    }

    private String body(byte[] content, String encoding) {
        if (content == null || content.length == 0) {
            return "";
        }
        Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        String raw = new String(content, charset).replaceAll("\\s+", " ").trim();
        if (raw.length() <= MAX_LOGGED_BODY_LENGTH) {
            return raw;
        }
        return raw.substring(0, MAX_LOGGED_BODY_LENGTH) + "...";
    }
}
