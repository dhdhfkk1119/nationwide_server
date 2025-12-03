package com.nationwide.nationwide_server.core.jwt;

import com.nationwide.nationwide_server.member.Member;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {
    private SecretKey key = Jwts.SIG.HS256.key().build(); // 대칭키 생성
    private final long accessExpirationMs; // 일반 토큰 유효 시간 1시간
    private final long refreshExpirationMs; // 새 토큰 유효 시간 70일


    public JwtTokenProvider(
            @Value("${jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    // Access Token 생성
    public String createAccessToken(Member member) {
        log.debug("Access Token 생성 시작 - 사용자: {}", member.getLoginId());
        return createToken(member, accessExpirationMs);
    }

    // Access Token 1시간
    public Long getAccessExpirationSeconds() {
        return accessExpirationMs / 1000;  // 밀리초 → 초
    }

    // Refresh Token 생성
    public String createRefreshToken(Member member) {
        log.debug("Refresh Token 생성 시작 - 사용자: {}", member.getLoginId());
        return createToken(member, refreshExpirationMs);
    }

    // 토큰 생성 공통 로직
    private String createToken(Member member, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", member.getId());
        claims.put("name", member.getName());
        claims.put("profileImage", member.getProfileImage());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
                .subject(member.getLoginId())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();

        log.info("토큰 생성 완료 - 사용자: {}, 만료시간: {}", member.getLoginId(), expiry);
        return token;
    }

    // 토큰 검증 하기
    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (Exception e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // 생성한 토큰 추출
    public Member getClaimsMember(String token) {
        log.debug("토큰에서 회원 정보 추출 시작");
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Claims에서 개별 필드를 꺼내서 Member 객체 생성
            Member member = new Member();
            member.setId(claims.get("id", Long.class));
            member.setLoginId(claims.getSubject());
            member.setName(claims.get("name", String.class));
            member.setProfileImage(claims.get("profileImage", String.class));

            log.debug("회원 정보 추출 완료 - ID: {}", member.getId());
            return member;

        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰에서 정보 추출 시도");
            // 만료된 토큰이라도 Claims는 추출 가능
            Claims claims = e.getClaims();

            Member member = new Member();
            member.setId(claims.get("id", Long.class));
            member.setLoginId(claims.getSubject());
            member.setName(claims.get("name", String.class));
            member.setProfileImage(claims.get("profileImage", String.class));

            return member;

        } catch (Exception e) {
            log.error("토큰에서 회원 정보 추출 실패", e);
            return null;
        }
    }

    // 유저 loginId 가져오기
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
            log.error("LoginId 추출 실패", e);
            return null;
        }
    }

}
