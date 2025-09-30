package io.acofitec.security;

import java.util.List;
import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "acofitec.security.mock")
public interface MockSecurityConfig {

    @WithDefault("false")
    boolean enabled();

    @WithDefault("dev-user")
    String username();

    @WithDefault("user")
    List<String> roles();

    Optional<String> email();

    Optional<String> subject();
}
