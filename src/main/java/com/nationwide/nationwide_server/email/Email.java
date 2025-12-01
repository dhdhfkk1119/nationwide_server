package com.nationwide.nationwide_server.email;


import com.nationwide.nationwide_server.core.errors.exception.Exception400;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "email_tb")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String code;

    private boolean verified = false;

    private LocalDateTime expiredAt;

    private Integer attemptCount = 0;

    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public boolean isCodeMatch(String inputCode) {
        return this.code.equals(inputCode);
    }

    public void incrementAttempt() {
        this.attemptCount++;
        if(this.attemptCount >= 5) {
            throw new Exception400("인증 시도 횟수를 초과했습니다");
        }
    }
}
