package com.nationwide.nationwide_server.member;

import com.nationwide.nationwide_server.core.util.TimeFormatUtil;
import com.nationwide.nationwide_server.member.m_enum.Gender;
import com.nationwide.nationwide_server.member.m_enum.LoginType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member_tb")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String nickName;

    private String loginId;
    private String password;

    private String phoneNumber;
    private String email;
    private boolean phoneVerified;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String birth; // 생년
    private String date; // 월 일

    private String addressNumber; // 지번
    private String address; // 주소
    private String addressDetail; // 상세 주소

    private String profileImage; // 프로필 이미지 -> fileUpload

    @Enumerated(EnumType.STRING)
    private LoginType LoginType; // 로그인 타입 설정

    @CreationTimestamp
    private Timestamp createdAt; // 생성 일

    public String getTime(){
        return TimeFormatUtil.timestampFormat(createdAt);
    }


    private boolean isEmailVerified; // 이메일 인증 유무
    private boolean isPhoneVerified; // 휴대폰 인증 유무

}
