package com.nationwide.nationwide_server._core.jwt;

import com.nationwide.nationwide_server.image_file.ImageFile;
import com.nationwide.nationwide_server.member.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {
    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String createAccessToken(Member member) {
        return createToken(member, accessExpirationMs);
    }

    public Long getAccessExpirationSeconds() {
        return accessExpirationMs / 1000;
    }

    public String createRefreshToken(Member member) {
        return createToken(member, refreshExpirationMs);
    }

    private String createToken(Member member, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", member.getId());
        claims.put("name", member.getName());

        String profileImagePath = member.getImageFiles().isEmpty()
                ? null
                : member.getImageFiles().getFirst().getImageFilePath();
        claims.put("profileImage", profileImagePath);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(member.getLoginId())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature", e);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token");
        } catch (Exception e) {
            log.error("Invalid JWT token");
        }
        return false;
    }

    public Member getClaimsMember(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return memberFromClaims(claims);
        } catch (ExpiredJwtException e) {
            return memberFromClaims(e.getClaims());
        } catch (Exception e) {
            log.error("Failed to parse member from token", e);
            return null;
        }
    }

    public String getLoginId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (Exception e) {
            log.error("Failed to parse loginId", e);
            return null;
        }
    }

    private Member memberFromClaims(Claims claims) {
        String profileImagePath = claims.get("profileImage", String.class);
        ImageFile imageFile = profileImagePath != null
                ? ImageFile.builder().imageFilePath(profileImagePath).build()
                : null;

        Member member = new Member();
        member.setId(claims.get("id", Long.class));
        member.setLoginId(claims.getSubject());
        member.setName(claims.get("name", String.class));
        if (imageFile != null) {
            member.getImageFiles().add(imageFile);
        }
        return member;
    }
}
