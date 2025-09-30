package io.acofitec.security;

import java.security.Principal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.security.runtime.SecurityIdentityAssociation;

/**
 * Provides a deterministic security identity while developing or executing tests
 * without depending on an external OIDC provider. The identity can be customised
 * through HTTP headers so that different scenarios can be simulated locally.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
@IfBuildProfile(anyOf = { "dev", "test" })
public class MockSecurityFilter implements ContainerRequestFilter {

    public static final String USER_HEADER = "X-Mock-User";
    public static final String ROLES_HEADER = "X-Mock-Roles";
    public static final String TOKEN_HEADER = "Authorization";

    private final SecurityIdentityAssociation identityAssociation;
    private final MockSecurityConfig config;

    @Inject
    public MockSecurityFilter(SecurityIdentityAssociation identityAssociation, MockSecurityConfig config) {
        this.identityAssociation = identityAssociation;
        this.config = config;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!config.enabled()) {
            return;
        }

        QuarkusSecurityIdentity.Builder identity = QuarkusSecurityIdentity.builder();
        identity.setPrincipal(new SimplePrincipal(resolveUsername(requestContext)));
        identity.setAnonymous(false);
        resolveRoles(requestContext).forEach(identity::addRole);

        String token = extractBearerToken(requestContext);
        if (token != null) {
            identity.addAttribute("token", token);
        }

        config.email().ifPresent(email -> identity.addAttribute("email", email));
        config.subject().ifPresent(subject -> identity.addAttribute("subject", subject));

        identityAssociation.setIdentity(identity.build());
    }

    private String resolveUsername(ContainerRequestContext context) {
        String fromHeader = context.getHeaderString(USER_HEADER);
        if (fromHeader != null && !fromHeader.isBlank()) {
            return fromHeader.trim();
        }
        return config.username();
    }

    private Set<String> resolveRoles(ContainerRequestContext context) {
        String fromHeader = context.getHeaderString(ROLES_HEADER);
        if (fromHeader == null || fromHeader.isBlank()) {
            return new LinkedHashSet<>(config.roles());
        }
        Set<String> roles = new LinkedHashSet<>();
        Arrays.stream(fromHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(roles::add);
        return roles;
    }

    private String extractBearerToken(ContainerRequestContext context) {
        String authorization = context.getHeaderString(TOKEN_HEADER);
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        if (authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = authorization.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return authorization.trim();
    }

    private static final class SimplePrincipal implements Principal {
        private final String name;

        private SimplePrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
