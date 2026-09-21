package com.creatorhire.config;

import com.creatorhire.entity.Role;
import com.creatorhire.repository.RoleRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds the fixed role catalog (CLIENT / CREATOR / ADMIN) on startup.
 * Idempotent: only inserts missing roles.
 */
@Configuration
public class RoleSeeder {

    @Bean
    ApplicationRunner seedRoles(RoleRepository roles) {
        return args -> {
            for (String name : new String[] {"CLIENT", "CREATOR", "ADMIN"}) {
                if (roles.findByName(name).isEmpty()) {
                    roles.save(new Role(name));
                }
            }
        };
    }
}
