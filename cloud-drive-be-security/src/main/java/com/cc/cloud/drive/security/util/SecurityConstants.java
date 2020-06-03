package com.cc.cloud.drive.security.util;

public class SecurityConstants {
    public static final String AUTH_LOGIN_URL = "/users/authenticate";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";
    public static final String SECRET_KEY = "H@McQfTjWnZr4u7x!z%C*F-JaNdRgUkXp2s5v8y/B?D(G+KbPeShVmYq3t6w9z$C";
    public static final String TOKEN_ROLES = "roles";
    public static final Integer TOKEN_VALIDITY = 5 * 60 * 60 * 1000; // millis

    private SecurityConstants() {
        throw new IllegalStateException("Cannot create instance of static util class");
    }
}
