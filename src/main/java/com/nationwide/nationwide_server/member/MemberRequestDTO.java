package com.nationwide.nationwide_server.member;

import lombok.Data;

public class MemberRequestDTO {

    @Data
    public static class SaveDTO{
        private String name;
        private String nickName;

        private String MemberId;
        private String password;
        private String rePassword;

        private String phoneNumber;
        private Gender gender;

        private String birth; // 생년
        private String date; // 월 일

        private String addressNumber;
        private String address;
        private String addressDetail;

        public Member toEntity(){
            return Member.builder()
                    .name(this.name)
                    .nickName(this.nickName)
                    .MemberId(this.MemberId)
                    .password(this.password)
                    .phoneNumber(this.phoneNumber)
                    .gender(this.gender)
                    .birth(this.birth)
                    .date(this.date)
                    .addressNumber(this.addressNumber)
                    .address(this.address)
                    .addressDetail(this.addressDetail)
                    .build();
        }
    }
}
