package com.nationwide.nationwide_server.member.dto;

import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.m_enum.Gender;
import lombok.Data;

public class MemberResponseDTO {

    @Data
    public static class DetailDTO{
        private String name;
        private String phoneNumber;
        private String createdAt;
        private Gender gender;
        private String birth;
        private String date;
        private String addressNumber;
        private String address;
        private String addressDetail;
        private String profileImage;

        public DetailDTO(Member member){
            this.name = member.getName();
            this.phoneNumber = member.getPhoneNumber();
            this.createdAt = member.getTime();
            this.gender = member.getGender();
            this.birth = member.getBirth();
            this.date = member.getDate();
            this.addressNumber = member.getAddressNumber();
            this.address = member.getAddress();
            this.addressDetail = member.getAddressDetail();
            this.profileImage = member.getProfileImage();
        }
    }

    @Data
    public static class ListDTO{
        private String name;
        private String phoneNumber;
        private String createdAt;
        private Gender gender;
        private String birth;
        private String date;
        private String addressNumber;
        private String address;
        private String addressDetail;
        private String profileImage;

        public ListDTO(Member member){
            this.name = member.getName();
            this.phoneNumber = member.getPhoneNumber();
            this.createdAt = member.getTime();
            this.gender = member.getGender();
            this.birth = member.getBirth();
            this.date = member.getDate();
            this.addressNumber = member.getAddressNumber();
            this.address = member.getAddress();
            this.addressDetail = member.getAddressDetail();
            this.profileImage = member.getProfileImage();
        }
    }

    @Data
    public static class LoginDTO{

        private String accessToken;
        private String refreshToken;
        private Long expiresIn;

        private Long id;
        private String name;
        private String nickName;
        private String phoneNumber;
        private String createdAt;
        private Gender gender;
        private String birth;
        private String date;
        private String addressNumber;
        private String address;
        private String addressDetail;
        private String profileImage;


        public LoginDTO(Member member,String accessToken, String refreshToken,Long expiresIn){
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.id = member.getId();
            this.name = member.getName();
            this.nickName = member.getNickName();
            this.phoneNumber = member.getPhoneNumber();
            this.createdAt = member.getTime();
            this.gender = member.getGender();
            this.birth = member.getBirth();
            this.date = member.getDate();
            this.addressNumber = member.getAddressNumber();
            this.address = member.getAddress();
            this.addressDetail = member.getAddressDetail();
            this.profileImage = member.getProfileImage();
        }
    }
}
