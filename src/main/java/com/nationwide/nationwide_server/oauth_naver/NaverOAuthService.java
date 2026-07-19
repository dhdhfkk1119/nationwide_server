package com.nationwide.nationwide_server.oauth_naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nationwide.nationwide_server._core.errors.exception.Exception400;
import com.nationwide.nationwide_server._core.jwt.JwtTokenProvider;
import com.nationwide.nationwide_server.email.EmailService;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberRepository;
import com.nationwide.nationwide_server.member.m_enum.LoginType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuthService {
    private final RestClient naverOAuthRestClient;
    private final NaverOAuthProperties naverOAuthProperties;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public String buildAuthorizeUrl(String state) {
        if (!naverOAuthProperties.isConfigured()) {
            throw new Exception400("네이버 로그인이 설정되어 있지 않습니다.");
        }

        return UriComponentsBuilder.fromUriString("https://nid.naver.com/oauth2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", naverOAuthProperties.getClientId())
                .queryParam("redirect_uri", naverOAuthProperties.getRedirectUri())
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Transactional
    public LoginResult loginWithNaver(String code, String state, String ipAddress) {
        String naverAccessToken = exchangeToken(code, state);
        NaverProfile profile = fetchProfile(naverAccessToken);
        Member member = findOrCreateMember(profile);
        emailService.notifyIfNewLoginLocation(member, ipAddress);

        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);
        return new LoginResult(accessToken, refreshToken);
    }

    private String exchangeToken(String code, String state) {
        URI uri = UriComponentsBuilder.fromUriString("https://nid.naver.com/oauth2.0/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", naverOAuthProperties.getClientId())
                .queryParam("client_secret", naverOAuthProperties.getClientSecret())
                .queryParam("code", code)
                .queryParam("state", state)
                .build()
                .toUri();

        NaverTokenResponse response;
        try {
            response = naverOAuthRestClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(NaverTokenResponse.class);
        } catch (Exception exception) {
            log.error("네이버 토큰 교환에 실패했습니다.", exception);
            throw new Exception400("네이버 인증에 실패했습니다.");
        }

        if (response == null || response.accessToken() == null) {
            throw new Exception400("네이버 인증에 실패했습니다.");
        }
        return response.accessToken();
    }

    private NaverProfile fetchProfile(String naverAccessToken) {
        NaverProfileResponse response;
        try {
            response = naverOAuthRestClient.get()
                    .uri("https://openapi.naver.com/v1/nid/me")
                    .header("Authorization", "Bearer " + naverAccessToken)
                    .retrieve()
                    .body(NaverProfileResponse.class);
        } catch (Exception exception) {
            log.error("네이버 프로필 조회에 실패했습니다.", exception);
            throw new Exception400("네이버 프로필 조회에 실패했습니다.");
        }

        if (response == null || response.response() == null || response.response().id() == null) {
            throw new Exception400("네이버 프로필 조회에 실패했습니다.");
        }
        return response.response();
    }

    private Member findOrCreateMember(NaverProfile profile) {
        String loginId = "naver_" + profile.id();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .loginId(loginId)
                                .name(profile.name() != null && !profile.name().isBlank() ? profile.name() : profile.nickname())
                                .nickName(profile.nickname())
                                .email(profile.email())
                                .loginType(LoginType.NAVER)
                                .isEmailVerified(true)
                                .build()
                ));

        // email 컬럼 도입 이전에 가입한 회원은 email이 비어있을 수 있으므로, 로그인 시점에 채워준다.
        if ((member.getEmail() == null || member.getEmail().isBlank())
                && profile.email() != null && !profile.email().isBlank()) {
            member.setEmail(profile.email());
        }

        return member;
    }

    public record LoginResult(String accessToken, String refreshToken) {
    }

    private record NaverTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") String expiresIn
    ) {
    }

    private record NaverProfileResponse(String resultcode, String message, NaverProfile response) {
    }

    private record NaverProfile(
            String id,
            String email,
            String name,
            String nickname
    ) {
    }
}
