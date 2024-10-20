package com.rapidmotos.rapidmotos.Config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.rapidmotos.rapidmotos.Utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

public class JwtTokenValidator extends OncePerRequestFilter /*Ejecuta filtro por cada petición*/ {

    private final JwtUtils jwtUtils;

    public JwtTokenValidator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            DecodedJWT decoded = jwtUtils.verifyToken(token);

            //CONCEDER ACCESO AL USUARIO
            String username = jwtUtils.extractUser(decoded);
            String stringAuthorities = jwtUtils.getClaim(decoded,"authorities").asString();
            Long empresId = jwtUtils.getClaim(decoded,"employeeId").asLong();

            //Devolver los permisos separados por coma
            Collection<? extends GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(stringAuthorities);

            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }

        //Si no hay token sigue con el siguiente filtro
        filterChain.doFilter(request,response);
    }
}
