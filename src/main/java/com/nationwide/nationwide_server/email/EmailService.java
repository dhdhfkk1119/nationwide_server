package com.nationwide.nationwide_server.email;

import com.nationwide.nationwide_server.core.errors.exception.Exception400;
import com.nationwide.nationwide_server.core.errors.exception.Exception404;
import com.nationwide.nationwide_server.core.errors.exception.Exception500;
import com.nationwide.nationwide_server.member.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailService {
    private final EmailRepository emailRepository;
    private final MemberRepository memberRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public void sendVerificationCode(String loginId){

        if(memberRepository.existsByLoginId(loginId)){
            throw new Exception400("이미 가입 된 이메일 입니다");
        }

        emailRepository.deleteByLoginId(loginId);

        String code = generateRandomCode();

        Email verification = Email.builder()
                .loginId(loginId)
                .code(code)
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        emailRepository.save(verification);

        // 5. 이메일 발송
        sendEmail(loginId, code);
    }

    @Transactional
    public void verifyCode(String loginId, String code) {

        // 1. EmailVerification 조회
        Email verification = findByLoginId(loginId);

        // 2. 이미 인증됐는지 확인
        if(verification.isVerified()) {
            throw new Exception400("이미 인증된 이메일입니다");
        }

        // 3. 만료 확인
        if(verification.isExpired()) {
            throw new Exception400("인증 코드가 만료되었습니다. 재발송 해주세요");
        }

        // 4. 코드 일치 확인
        if(!verification.isCodeMatch(code)) {
            verification.incrementAttempt();  // 시도 횟수 증가
            emailRepository.save(verification);
            throw new Exception400("인증 코드가 일치하지 않습니다");
        }

        // 5. 인증 완료 처리
        verification.setVerified(true);
        emailRepository.save(verification);
    }

    // 다시 전송
    @Transactional
    public void resendVerificationCode(String email) {
        // 기존 코드 삭제하고 새로 발송
        sendVerificationCode(email);
    }

    // 랜덤 코드 전송 6자리
    private String generateRandomCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }


    // 이메일 인증 전송 형태 G-Mail 보이는 형식
    private void sendEmail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[NationWide] 이메일 인증 코드");
            helper.setText(buildEmailContent(code), true);  // HTML 형식

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new Exception500("이메일 발송에 실패했습니다");
        }
    }

    // 이메일 내용 (HTML)
    private String buildEmailContent(String code) {
        return "<div style='padding: 20px;'>" +
                "<h2>이메일 인증</h2>" +
                "<p>아래 인증 코드를 입력해주세요.</p>" +
                "<div style='background-color: #f0f0f0; padding: 15px; font-size: 24px; font-weight: bold;'>" +
                code +
                "</div>" +
                "<p>인증 코드는 5분간 유효합니다.</p>" +
                "</div>";
    }

    public Email findByLoginId(String loginId){
      return emailRepository.findByEmail(loginId).orElseThrow(() -> new Exception404("유효한 이메일이 아닙니다"));
    }
}
