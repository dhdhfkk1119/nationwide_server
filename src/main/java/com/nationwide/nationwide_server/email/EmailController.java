package com.nationwide.nationwide_server.email;

import com.nationwide.nationwide_server.core.util.ApiUtil;
import com.nationwide.nationwide_server.email.dto.EmailRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    // 이메일 인증 코드 발송
    @PostMapping("/signup/request")
    public ResponseEntity<?> signupRequest(
            @Valid @RequestBody EmailRequestDTO request) {

        emailService.sendVerificationCode(request.getEmail());

        return ResponseEntity.ok()
                .body(ApiUtil.success("인증 코드를 이메일로 발송했습니다"));
    }

    // 인증 코드 확인
    @PostMapping("/signup/verify")
    public ResponseEntity<?> verifyCode(
            @Valid @RequestBody EmailRequestDTO request) {

        emailService.verifyCode(request.getEmail(), request.getCode());

        return ResponseEntity.ok()
                .body(ApiUtil.success( true));
    }

    // 인증 코드 재전송
    @PostMapping("/signup/resend")
    public ResponseEntity<?> resendCode(
            @Valid @RequestBody EmailRequestDTO request) {

        emailService.resendVerificationCode(request.getEmail());

        return ResponseEntity.ok()
                .body(ApiUtil.success("인증 코드를 재발송했습니다"));
    }
}
