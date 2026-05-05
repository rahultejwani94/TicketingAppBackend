package com.example.demo.utilities;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "my-super-secret-key-my-super-secret-key";
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generateToken(String username) {

        return Jwts.builder()
                .setSubject(username)   // ✅ old method
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key, SignatureAlgorithm.HS256) // ✅ required here
                .compact();
    }

    public static String validateToken(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)   // ✅ correct for 0.11.x
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}