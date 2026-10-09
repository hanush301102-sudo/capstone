package com.creatorhire.security;

import com.creatorhire.exception.RateLimitException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int maxRequests;
    private final long windowSeconds;
    private final boolean enabled;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${app.rate-limit.max-requests:10}") int maxRequests,
            @Value("${app.rate-limit.window-seconds:60}") long windowSeconds,
            @Value("${app.rate-limit.enabled:true}") boolean enabled) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.enabled = enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (!path.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr();
        Window window = windows.compute(key, (k, w) -> {
            if (w == null || w.isExpired()) {
                return new Window();
            }
            return w;
        });

        int count = window.increment();
        if (count > maxRequests) {
            throw new RateLimitException("Too many requests. Please try again later");
        }

        chain.doFilter(request, response);
    }

    private class Window {
        private final Instant start = Instant.now();
        private final AtomicInteger count = new AtomicInteger(0);

        int increment() {
            return count.incrementAndGet();
        }

        boolean isExpired() {
            return Instant.now().isAfter(start.plusSeconds(windowSeconds));
        }
    }
}
