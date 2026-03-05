package com.nationwide.nationwide_server.member.dto;

import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.m_enum.Gender;
import lombok.Data;

import java.util.List;

public class MemberResponseDTO {

    @Data
    public static class DetailDTO{
        private Long memberIdx;
        private String name;
        private String nickName;
        private String phoneNumber;
        private String createdAt;
        private Gender gender;
        private String birth;
        private String date;
        private String addressInfo;
        private String thumbnailProfileImagePath;
        private List<String> profileImagePath;
        private List<String> imageFilesId;



        public DetailDTO(Member member, List<ImageResponseDTO> imageFiles){
            this.memberIdx = member.getId();
            this.name = member.getName();
            this.nickName = member.getNickName();
            this.phoneNumber = member.getPhoneNumber();
            this.createdAt = member.getTime();
            this.gender = member.getGender();
            this.birth = member.getBirth();
            this.date = member.getDate();
            this.addressInfo = member.getAddress() + member.getAddressDetail() + member.getAddressNumber();
            this.thumbnailProfileImagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath).findFirst().orElse("등록된 썸네일 이미지가 없습니다");
            this.profileImagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath)
                    .toList();
            this.imageFilesId = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFileId)
                    .toList();
        }
    }

    @Data
    public static class ListDTO{
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
        private String addressInfo;
        private String thumbnailProfileImagePath;
        private List<String> profileImagePath;
        private List<String> imageFilesId;


        public ListDTO(Member member,List<ImageResponseDTO> imageFiles){
            this.name = member.getName();
            this.nickName = member.getNickName();
            this.phoneNumber = member.getPhoneNumber();
            this.createdAt = member.getTime();
            this.gender = member.getGender();
            this.birth = member.getBirth();
            this.date = member.getDate();
            this.addressInfo = member.getAddress() + member.getAddressDetail() + member.getAddressNumber();
            this.thumbnailProfileImagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath).findFirst().orElse("등록된 썸네일 이미지가 없습니다");
            this.profileImagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath)
                    .toList();
            this.imageFilesId = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFileId)
                    .toList();
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
        private String addressInfo;
        private String addressNumber;
        private String address;
        private String addressDetail;
        private String thumbnailProfileImagePath;



        public LoginDTO(Member member,String accessToken, String refreshToken,Long expiresIn,String thumbnailProfileImagePath){
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
            this.addressInfo = member.getAddress() + member.getAddressDetail() + member.getAddressNumber();
            this.addressNumber = member.getAddressNumber();
        }
    }
}
