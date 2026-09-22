package com.creatorhire.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the React SPA (bundled under {@code static/}) for all frontend
 * routes so the backend port delivers the whole website: pages + API.
 * API, actuator, docs, and H2 console paths are never matched here.
 */
@Controller
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
