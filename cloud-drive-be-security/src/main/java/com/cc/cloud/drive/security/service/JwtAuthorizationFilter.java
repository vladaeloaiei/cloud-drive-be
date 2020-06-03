package com.cc.cloud.drive.security.service;

import com.cc.cloud.drive.security.util.JwtUtil;
import com.cc.cloud.drive.security.util.SecurityConstants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static java.util.Objects.nonNull;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    @Autowired
    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.TOKEN_HEADER);

        if (isValidToken(token)) {
            try {
                return getAuthenticationFromToken(token);
            } catch (ExpiredJwtException e) {
                LOGGER.warn("Request to parse expired JWT : {} failed : {}", token, e.getMessage(), e);
            } catch (UnsupportedJwtException e) {
                LOGGER.warn("Request to parse unsupported JWT : {} failed : {}", token, e.getMessage(), e);
            } catch (MalformedJwtException e) {
                LOGGER.warn("Request to parse invalid JWT : {} failed : {}", token, e.getMessage(), e);
            } catch (SignatureException e) {
                LOGGER.warn("Request to parse JWT with invalid signature : {} failed : {}", token, e.getMessage(), e);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Request to parse empty or null JWT : {} failed : {}", token, e.getMessage(), e);
            }
        }

        return null;
    }

    private boolean isValidToken(String token) {
        return nonNull(token) && token.startsWith(SecurityConstants.TOKEN_PREFIX);
    }

    private UsernamePasswordAuthenticationToken getAuthenticationFromToken(String token) {
        Pair<String, List<GrantedAuthority>> userDetails = JwtUtil.parseToken(token);
        return new UsernamePasswordAuthenticationToken(userDetails.getKey(), null, userDetails.getValue());
    }
}
