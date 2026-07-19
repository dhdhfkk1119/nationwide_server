package com.nationwide.nationwide_server.oauth_kakao;

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
public class KakaoOAuthService {
    private final RestClient kakaoOAuthRestClient;
    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public String buildAuthorizeUrl(String state) {
        if (!kakaoOAuthProperties.isConfigured()) {
            throw new Exception400("카카오 로그인이 설정되어 있지 않습니다.");
        }

        return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", kakaoOAuthProperties.getClientId())
                .queryParam("redirect_uri", kakaoOAuthProperties.getRedirectUri())
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Transactional
    public LoginResult loginWithKakao(String code, String ipAddress) {
        String kakaoAccessToken = exchangeToken(code);
        KakaoProfile profile = fetchProfile(kakaoAccessToken);
        Member member = findOrCreateMember(profile);
        // 카카오는 이메일 동의 항목을 받지 않아 member.getEmail()이 없으므로 IP만 기록되고 알림은 조용히 건너뛴다.
        emailService.notifyIfNewLoginLocation(member, ipAddress);

        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);
        return new LoginResult(accessToken, refreshToken);
    }

    private String exchangeToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", kakaoOAuthProperties.getClientId());
        form.add("client_secret", kakaoOAuthProperties.getClientSecret());
        form.add("redirect_uri", kakaoOAuthProperties.getRedirectUri());
        form.add("code", code);

        KakaoTokenResponse response;
        try {
            response = kakaoOAuthRestClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
        } catch (Exception exception) {
            log.error("카카오 토큰 교환에 실패했습니다.", exception);
            throw new Exception400("카카오 인증에 실패했습니다.");
        }

        if (response == null || response.accessToken() == null) {
            throw new Exception400("카카오 인증에 실패했습니다.");
        }
        return response.accessToken();
    }

    private KakaoProfile fetchProfile(String kakaoAccessToken) {
        KakaoUserResponse response;
        try {
            response = kakaoOAuthRestClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);
        } catch (Exception exception) {
            log.error("카카오 프로필 조회에 실패했습니다.", exception);
            throw new Exception400("카카오 프로필 조회에 실패했습니다.");
        }

        if (response == null || response.id() == null) {
            throw new Exception400("카카오 프로필 조회에 실패했습니다.");
        }

        String nickname = response.kakaoAccount() != null && response.kakaoAccount().profile() != null
                ? response.kakaoAccount().profile().nickname()
                : null;

        return new KakaoProfile(response.id(), nickname);
    }

    private Member findOrCreateMember(KakaoProfile profile) {
        String loginId = "kakao_" + profile.id();

        return memberRepository.findByLoginId(loginId)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .loginId(loginId)
                                .name(profile.nickname() != null && !profile.nickname().isBlank() ? profile.nickname() : loginId)
                                .nickName(profile.nickname())
                                .loginType(LoginType.KAKAO)
                                .isEmailVerified(true)
                                .build()
                ));
    }

    public record LoginResult(String accessToken, String refreshToken) {
    }

    private record KakaoProfile(Long id, String nickname) {
    }

    private record KakaoTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Integer expiresIn
    ) {
    }

    private record KakaoUserResponse(
            Long id,
            @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {
    }

    private record KakaoAccount(KakaoProfileInfo profile) {
    }

    private record KakaoProfileInfo(String nickname) {
    }
}
