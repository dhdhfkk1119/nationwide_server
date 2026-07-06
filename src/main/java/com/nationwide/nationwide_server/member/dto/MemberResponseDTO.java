package com.nationwide.nationwide_server.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.m_enum.Gender;
import lombok.Data;

import java.util.List;

public class MemberResponseDTO {

    @Data
    public static class DetailDTO {
        private Long memberIdx;
        private String name;
        private String nickName;
        private String phoneNumber;
        private String createdAt;
        private Gender gender;
        private String birth;
        private String date;
        private String addressInfo;
        private String bio;
        private Double latitude;
        private Double longitude;
        private int addressChangeCount;
        private String thumbnailProfileImagePath;
        private List<String> profileImagePath;
        private List<String> imageFilesId;
        private Long boardCnt;
        private Long followerCnt;
        private Long followingCnt;
        @JsonProperty("isPrivateProfile")
        private boolean isPrivateProfile;
        @JsonProperty("isLocationVisible")
        private boolean isLocationVisible;

        public DetailDTO(
                Member member,
                List<ImageResponseDTO> imageFiles,
                Long boardCnt,
                Long followerCnt,
                Long followingCnt
        ) {
            this.memberIdx = member.getId();
            this.name = member.getName();
            this.nickName = member.getNickName();
            this.phoneNumber = member.getPhoneNumber();
            this.createdAt = member.getTime();
            this.gender = member.getGender();
            this.birth = member.getBirth();
            this.date = member.getDate();
            this.addressInfo = member.getAddress() + member.getAddressDetail() + member.getAddressNumber();
            this.bio = member.getBio();
            this.latitude = member.getLatitude();
            this.longitude = member.getLongitude();
            this.addressChangeCount = member.getAddressChangeCount();
            this.thumbnailProfileImagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath)
                    .findFirst()
                    .orElse("/uploads/member-images/profile.png");
            this.profileImagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath)
                    .toList();
            this.imageFilesId = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFileId)
                    .toList();
            this.boardCnt = boardCnt;
            this.followerCnt = followerCnt;
            this.followingCnt = followingCnt;
            this.isPrivateProfile = member.isPrivateProfile();
            this.isLocationVisible = member.isLocationVisible();
        }
    }

    @Data
    public static class ListDTO {
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
        private String bio;
        private Double latitude;
        private Double longitude;
        private int addressChangeCount;
        private String thumbnailProfileImagePath;
        @JsonProperty("isPrivateProfile")
        private boolean isPrivateProfile;
        @JsonProperty("isLocationVisible")
        private boolean isLocationVisible;
        private List<String> profileImagePath;
        private List<String> imageFilesId;

        public ListDTO(Member member, List<ImageResponseDTO> imageFiles) {
            this.name = member.getName();
            this.nickName = member.getNickName();
            this.phoneNumber = member.getPhoneNumber();
            this.createdAt = member.getTime();
            this.gender = member.getGender();
            this.birth = member.getBirth();
            this.date = member.getDate();
            this.addressInfo = member.getAddress() + member.getAddressDetail() + member.getAddressNumber();
            this.bio = member.getBio();
            this.latitude = member.getLatitude();
            this.longitude = member.getLongitude();
            this.addressChangeCount = member.getAddressChangeCount();
            this.thumbnailProfileImagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath)
                    .findFirst()
                    .orElse("/uploads/member-images/profile.png");
            this.profileImagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath)
                    .toList();
            this.imageFilesId = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFileId)
                    .toList();
        }
    }

    @Data
    public static class LoginDTO {
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
        private String bio;
        private Double latitude;
        private Double longitude;
        private int addressChangeCount;
        private String thumbnailProfileImagePath;
        @JsonProperty("isPrivateProfile")
        private boolean isPrivateProfile;
        @JsonProperty("isLocationVisible")
        private boolean isLocationVisible;

        public LoginDTO(Member member, String accessToken, String refreshToken, Long expiresIn, String thumbnailProfileImagePath) {
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
            this.address = member.getAddress();
            this.addressDetail = member.getAddressDetail();
            this.bio = member.getBio();
            this.latitude = member.getLatitude();
            this.longitude = member.getLongitude();
            this.addressChangeCount = member.getAddressChangeCount();
            this.thumbnailProfileImagePath = thumbnailProfileImagePath;
            this.isPrivateProfile = member.isPrivateProfile();
            this.isLocationVisible = member.isLocationVisible();
        }
    }

    @Data
    public static class PrivacySettingsDTO {
        @JsonProperty("isPrivateProfile")
        private boolean isPrivateProfile;
        @JsonProperty("isLocationVisible")
        private boolean isLocationVisible;

        public static PrivacySettingsDTO of(Member member) {
            PrivacySettingsDTO dto = new PrivacySettingsDTO();
            dto.isPrivateProfile = member.isPrivateProfile();
            dto.isLocationVisible = member.isLocationVisible();
            return dto;
        }
    }
}
