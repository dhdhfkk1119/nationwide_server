package com.nationwide.nationwide_server.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.m_enum.Gender;
import com.nationwide.nationwide_server.member.m_enum.LoginType;
import com.nationwide.nationwide_server.member.m_enum.MessagePermission;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

public class MemberRequestDTO {

    @Data
    public static class SaveDTO {
        private String name;
        private String nickName;

        private String loginId;
        private String password;
        private String rePassword;

        private String phoneNumber;
        private Gender gender;

        private String birth;
        private String date;

        private String addressNumber;
        private String address;
        private String addressDetail;
        private String bio;

        private List<Long> agreedTermsIds;

        public Member toEntity() {
            return Member.builder()
                    .name(this.name)
                    .nickName(this.nickName)
                    .loginId(this.loginId)
                    .password(this.password)
                    .phoneNumber(this.phoneNumber)
                    .gender(this.gender)
                    .birth(this.birth)
                    .date(this.date)
                    .addressNumber(this.addressNumber)
                    .address(this.address)
                    .addressDetail(this.addressDetail)
                    .bio(this.bio)
                    .build();
        }

        public LocalDate getBirthDate() {
            return LocalDate.of(
                    Integer.parseInt(birth),
                    Integer.parseInt(date.substring(0, 2)),
                    Integer.parseInt(date.substring(2, 4))
            );
        }
    }

    @Data
    public static class LoginDTO {
        private String loginId;
        private String password;
        private boolean autoLogin;
        private LoginType provider;
    }

    @Data
    public static class UpdateDTO {
        private String name;
        private String nickName;
        private String phoneNumber;
        private Gender gender;
        private String birth;
        private String date;
        private String addressNumber;
        private String address;
        private String addressDetail;
        private String bio;
        private List<String> imageFileId;
        private Boolean isPrivateProfile;
        private Boolean isLocationVisible;
    }

    @Data
    public static class PrivacySettingsDTO {
        @JsonProperty("isPrivateProfile")
        private boolean isPrivateProfile;
        @JsonProperty("isLocationVisible")
        private boolean isLocationVisible;
        private MessagePermission messagePermission;
    }

    @Data
    public static class DeactivateRequestDTO {
        private int durationMonths;
    }

    @Data
    public static class CurrentLocationDTO {
        private Double latitude;
        private Double longitude;
    }

    @Data
    public static class ManualLocationDTO {
        private String fullAddress;
        private String address;
        private String address1;
        private String address2;
    }

    @Data
    public static class UserMe {
        private String accessToken;
        private String refreshToken;
    }
}
