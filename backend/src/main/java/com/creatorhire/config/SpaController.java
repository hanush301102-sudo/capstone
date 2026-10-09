package com.creatorhire.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the React SPA (bundled under {@code static/}) for all frontend
 * routes so the backend port delivers the whole website: pages + API.
 * API, actuator, docs, and H2 console paths are never matched here.
 *
 * <p>Only active when {@code app.spa.enabled=true}. For multi-origin
 * deployments (e.g. Vercel frontend + Render backend), leave this disabled
 * so the backend only serves API responses.
 */
@Controller
@ConditionalOnProperty(name = "app.spa.enabled", havingValue = "true", matchIfMissing = false)
public class SpaController {

    @GetMapping(value = {
        "/",
        "/discover",
        "/login",
        "/register",
        "/notifications",
        "/report",
        "/client",
        "/client/**",
        "/creator",
        "/creator/**",
        "/jobs/**",
        "/admin",
        "/admin/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
