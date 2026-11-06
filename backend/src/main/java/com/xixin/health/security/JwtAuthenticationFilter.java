package com.xixin.health.security;

import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Stream;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final UserAccountRepository userAccountRepository;

    public JwtAuthenticationFilter(TokenService tokenService, UserAccountRepository userAccountRepository) {
        this.tokenService = tokenService;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Support token in query param (for direct download links like export)
        if (authorization == null && request.getParameter("token") != null) {
            authorization = BEARER_PREFIX + request.getParameter("token");
        }
        if (authorization != null
            && authorization.startsWith(BEARER_PREFIX)
            && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = authorization.substring(BEARER_PREFIX.length()).trim();
            tokenService.authenticateAccessToken(token).ifPresent(authenticatedToken -> {
                userAccountRepository.findById(authenticatedToken.userId())
                    .filter(account -> account.isEnabled() && !account.isDeleted())
                    .ifPresent(account -> authenticate(request, authenticatedToken, account));
            });
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(
        HttpServletRequest request,
        TokenService.AuthenticatedToken authenticatedToken,
        UserAccount account
    ) {
        var authorities = account.getRoles().stream()
            .filter(role -> !role.isDeleted())
            .flatMap(role -> Stream.concat(
                Stream.of("ROLE_" + role.getCode()),
                role.getPermissions().stream().map(permission -> permission.getCode())
            ))
            .distinct()
            .map(SimpleGrantedAuthority::new)
            .toList();
        var authentication = new UsernamePasswordAuthenticationToken(
            authenticatedToken,
            null,
            authorities
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
