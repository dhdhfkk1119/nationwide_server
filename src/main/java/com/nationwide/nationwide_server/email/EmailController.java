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

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    // 이메일 인증 코드 발송
    @PostMapping("/send")
    public ResponseEntity<?> signupRequest(
            @Valid @RequestBody EmailRequestDTO request) {

        emailService.sendVerificationCode(request.getLoginId());

        return ResponseEntity.ok()
                .body(ApiUtil.success("인증 코드를 이메일로 발송했습니다"));
    }

    // 인증 코드 확인
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(
            @Valid @RequestBody EmailRequestDTO request) {

        emailService.verifyCode(request.getLoginId(), request.getCode());

        return ResponseEntity.ok()
                .body(ApiUtil.success( true));
    }

    // 인증 코드 재전송
    @PostMapping("/resend")
    public ResponseEntity<?> resendCode(
            @Valid @RequestBody EmailRequestDTO request) {

        emailService.resendVerificationCode(request.getLoginId());

        return ResponseEntity.ok()
                .body(ApiUtil.success("인증 코드를 재발송했습니다"));
    }
}
