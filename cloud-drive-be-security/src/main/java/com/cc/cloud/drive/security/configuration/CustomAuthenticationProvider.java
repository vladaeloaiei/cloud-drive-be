package com.cc.cloud.drive.security.configuration;

import com.cc.cloud.drive.security.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private UserService userService;

    @Autowired
    public CustomAuthenticationProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        if (areValidCredentials(username, password)) {
            return createAuthentication(username, password);
        } else {
            return null;
        }
    }

    private boolean areValidCredentials(String username, String password) {
        return nonNull(username) && nonNull(password) && userService.login(username, password);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(String username, String password) {
        return new UsernamePasswordAuthenticationToken(username, password, emptyList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
