package com.nationwide.nationwide_server.member.dto;

import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.m_enum.Gender;
import com.nationwide.nationwide_server.member.m_enum.LoginType;
import com.nationwide.nationwide_server.terms.Terms;
import com.nationwide.nationwide_server.terms.dto.TermsResponseDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberRequestDTO {

    @Data
    public static class SaveDTO{
        private String name;
        private String nickName;

        private String loginId;
        private String password;
        private String rePassword;

        private String phoneNumber;
        private Gender gender;
        private String email;

        private String birth; // 생년
        private String date; // 월 일

        private String addressNumber;
        private String address;
        private String addressDetail;

        private List<Long> agreedTermsIds;

        public Member toEntity(){
            return Member.builder()
                    .name(this.name)
                    .nickName(this.nickName)
                    .loginId(this.loginId)
                    .password(this.password)
                    .phoneNumber(this.phoneNumber)
                    .gender(this.gender)
                    .email(email)
                    .birth(this.birth)
                    .date(this.date)
                    .addressNumber(this.addressNumber)
                    .address(this.address)
                    .addressDetail(this.addressDetail)
                    .build();
        }

        public LocalDate getBirthDate() {
            return LocalDate.of(
                    Integer.parseInt(birth),
                    Integer.parseInt(date.substring(0, 2)), // 월
                    Integer.parseInt(date.substring(2, 4))  // 일
            );
        }
    }

    @Data
    public static class LoginDTO{
        private String loginId;
        private String password;
        private boolean autoLogin;
        private LoginType provider;
    }
}
