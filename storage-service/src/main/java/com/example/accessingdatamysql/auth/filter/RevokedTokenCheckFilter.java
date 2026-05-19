package com.example.accessingdatamysql.auth.filter;

import com.example.accessingdatamysql.auth.client.AuthRevocationClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RevokedTokenCheckFilter extends OncePerRequestFilter {

    private final AuthRevocationClient authRevocationClient;

    public RevokedTokenCheckFilter(AuthRevocationClient authRevocationClient) {
        this.authRevocationClient = authRevocationClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String jti = jwtAuthenticationToken.getToken().getClaimAsString("jti");
            if (authRevocationClient.isRevoked(jti)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been revoked");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}