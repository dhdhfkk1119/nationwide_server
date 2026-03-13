package com.nationwide.nationwide_server.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nationwide.nationwide_server.follow.FollowService;
import com.nationwide.nationwide_server.member.Member;
import lombok.Data;

public class FollowResponseDTO {
    @Data
    public static class StatusDTO {
        @JsonProperty("isFollowing")
        private boolean isFollowing;
        @JsonProperty("isFollower")
        private boolean isFollower;
        @JsonProperty("isFollow")
        private boolean isFollow;
        private Long followerCnt;
        private Long followingCnt;

        public static StatusDTO of(
                boolean isFollowing,
                boolean isFollower,
                Long followerCnt,
                Long followingCnt
        ) {
            StatusDTO dto = new StatusDTO();
            dto.isFollowing = isFollowing;
            dto.isFollower = isFollower;
            dto.isFollow = isFollowing && isFollower;
            dto.followerCnt = followerCnt;
            dto.followingCnt = followingCnt;
            return dto;
        }
    }

    @Data
    public static class MemberListDTO {
        private Long memberIdx;
        private String name;
        private String nickName;
        private String bio;
        private String thumbnailProfileImagePath;
        @JsonProperty("isFollowing")
        private boolean isFollowing;
        @JsonProperty("isFollower")
        private boolean isFollower;
        @JsonProperty("isFollow")
        private boolean isFollow;

        public static MemberListDTO of(Member member, Long viewerId, FollowService followService) {
            MemberListDTO dto = new MemberListDTO();
            dto.memberIdx = member.getId();
            dto.name = member.getName();
            dto.nickName = member.getNickName();
            dto.bio = member.getBio();
            dto.thumbnailProfileImagePath = member.getImageFiles().stream()
                    .findFirst()
                    .map(imageFile -> imageFile.getImageFilePath())
                    .orElse("/uploads/member-images/profile.png");

            StatusDTO status = followService.getStatus(viewerId, member.getId());
            dto.isFollowing = status.isFollowing;
            dto.isFollower = status.isFollower;
            dto.isFollow = status.isFollow;
            return dto;
        }
    }
}
