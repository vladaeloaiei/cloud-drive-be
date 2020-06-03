package com.cc.cloud.drive.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javafx.util.Pair;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.cc.cloud.drive.security.util.SecurityConstants.SECRET_KEY;
import static com.cc.cloud.drive.security.util.SecurityConstants.TOKEN_ROLES;
import static com.cc.cloud.drive.security.util.SecurityConstants.TOKEN_TYPE;
import static com.cc.cloud.drive.security.util.SecurityConstants.TOKEN_VALIDITY;

public class JwtUtil implements Serializable {
    public static String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .setHeaderParam("typ", TOKEN_TYPE)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .claim("roles", roles)
                .compact();
    }

    public static Pair<String, List<GrantedAuthority>> parseToken(String bearerToken) {
        Jws<Claims> jws = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(bearerToken.replace("Bearer ", ""));
        String username = jws.getBody().getSubject();
        List<GrantedAuthority> roles = ((List<?>) jws.getBody().get(TOKEN_ROLES)).stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .collect(Collectors.toList());

        return new Pair<>(username, roles);
    }

    private JwtUtil() {
        throw new IllegalStateException("Can not instantiate this class");
    }
}
