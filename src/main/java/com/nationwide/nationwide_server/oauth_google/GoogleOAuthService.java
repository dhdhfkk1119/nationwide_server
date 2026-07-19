package com.nationwide.nationwide_server.oauth_google;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nationwide.nationwide_server._core.errors.exception.Exception400;
import com.nationwide.nationwide_server._core.jwt.JwtTokenProvider;
import com.nationwide.nationwide_server.email.EmailService;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberRepository;
import com.nationwide.nationwide_server.member.m_enum.LoginType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthService {
    private static final String SCOPE = "openid email profile";

    private final RestClient googleOAuthRestClient;
    private final GoogleOAuthProperties googleOAuthProperties;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public String buildAuthorizeUrl(String state) {
        if (!googleOAuthProperties.isConfigured()) {
            throw new Exception400("구글 로그인이 설정되어 있지 않습니다.");
        }

        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("response_type", "code")
                .queryParam("client_id", googleOAuthProperties.getClientId())
                .queryParam("redirect_uri", googleOAuthProperties.getRedirectUri())
                .queryParam("scope", SCOPE)
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Transactional
    public LoginResult loginWithGoogle(String code, String ipAddress) {
        String googleAccessToken = exchangeToken(code);
        GoogleProfile profile = fetchProfile(googleAccessToken);
        Member member = findOrCreateMember(profile);
        emailService.notifyIfNewLoginLocation(member, ipAddress);

        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);
        return new LoginResult(accessToken, refreshToken);
    }

    private String exchangeToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", googleOAuthProperties.getClientId());
        form.add("client_secret", googleOAuthProperties.getClientSecret());
        form.add("redirect_uri", googleOAuthProperties.getRedirectUri());
        form.add("code", code);

        GoogleTokenResponse response;
        try {
            response = googleOAuthRestClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(GoogleTokenResponse.class);
        } catch (Exception exception) {
            log.error("구글 토큰 교환에 실패했습니다.", exception);
            throw new Exception400("구글 인증에 실패했습니다.");
        }

        if (response == null || response.accessToken() == null) {
            throw new Exception400("구글 인증에 실패했습니다.");
        }
        return response.accessToken();
    }

    private GoogleProfile fetchProfile(String googleAccessToken) {
        GoogleUserInfoResponse response;
        try {
            response = googleOAuthRestClient.get()
                    .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .header("Authorization", "Bearer " + googleAccessToken)
                    .retrieve()
                    .body(GoogleUserInfoResponse.class);
        } catch (Exception exception) {
            log.error("구글 프로필 조회에 실패했습니다.", exception);
            throw new Exception400("구글 프로필 조회에 실패했습니다.");
        }

        if (response == null || response.sub() == null) {
            throw new Exception400("구글 프로필 조회에 실패했습니다.");
        }

        return new GoogleProfile(response.sub(), response.name(), response.email());
    }

    private Member findOrCreateMember(GoogleProfile profile) {
        String loginId = "google_" + profile.sub();
        String displayName = profile.name() != null && !profile.name().isBlank()
                ? profile.name()
                : (profile.email() != null ? profile.email() : loginId);

        Member member = memberRepository.findByLoginId(loginId)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .loginId(loginId)
                                .name(displayName)
                                .nickName(profile.name())
                                .email(profile.email())
                                .loginType(LoginType.GOOGLE)
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

    private record GoogleProfile(String sub, String name, String email) {
    }

    private record GoogleTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Integer expiresIn
    ) {
    }

    private record GoogleUserInfoResponse(
            String sub,
            String email,
            String name,
            String picture
    ) {
    }
}
