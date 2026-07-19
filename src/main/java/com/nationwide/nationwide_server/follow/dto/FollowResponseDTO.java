package com.nationwide.nationwide_server.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nationwide.nationwide_server.follow.FollowRelationStatus;
import com.nationwide.nationwide_server.follow.FollowService;
import com.nationwide.nationwide_server.member.Member;
import lombok.Data;

public class FollowResponseDTO {
    private static final String DEACTIVATED_MEMBER_NAME = "비활성화된 계정";

    @Data
    public static class StatusDTO {
        @JsonProperty("isFollowing")
        private boolean isFollowing;
        @JsonProperty("isFollower")
        private boolean isFollower;
        @JsonProperty("isFollow")
        private boolean isFollow;
        private boolean hasPendingRequest;
        private boolean canViewProfile;
        private String relationStatus;
        private Long followerCnt;
        private Long followingCnt;
        // 내가 상대를 차단했는지
        private boolean isBlocking;
        // 상대가 나를 차단했는지
        private boolean isBlockedByOther;
        // 내가 상대에게 내 게시물을 숨겼는지
        private boolean isHidingFromOther;

        public static StatusDTO of(
                boolean isFollowing,
                boolean isFollower,
                boolean hasPendingRequest,
                boolean canViewProfile,
                FollowRelationStatus relationStatus,
                Long followerCnt,
                Long followingCnt,
                boolean isBlocking,
                boolean isBlockedByOther,
                boolean isHidingFromOther
        ) {
            StatusDTO dto = new StatusDTO();
            dto.isFollowing = isFollowing;
            dto.isFollower = isFollower;
            dto.isFollow = isFollowing && isFollower;
            dto.hasPendingRequest = hasPendingRequest;
            dto.canViewProfile = canViewProfile;
            dto.relationStatus = relationStatus != null ? relationStatus.name() : null;
            dto.followerCnt = followerCnt;
            dto.followingCnt = followingCnt;
            dto.isBlocking = isBlocking;
            dto.isBlockedByOther = isBlockedByOther;
            dto.isHidingFromOther = isHidingFromOther;
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
        private boolean hasPendingRequest;
        private boolean canViewProfile;
        private String relationStatus;

        public static MemberListDTO of(
                Member member,
                Long viewerId,
                FollowService followService,
                boolean hasPendingRequest
        ) {
            MemberListDTO dto = new MemberListDTO();
            boolean canExposeMember = !member.isDeactivatedNow() || (viewerId != null && viewerId.equals(member.getId()));
            dto.memberIdx = member.getId();
            dto.name = canExposeMember ? member.getName() : DEACTIVATED_MEMBER_NAME;
            dto.nickName = canExposeMember ? member.getDisplayNickName() : null;
            dto.bio = canExposeMember ? member.getBio() : null;
            dto.thumbnailProfileImagePath = canExposeMember
                    ? member.getImageFiles().stream()
                            .findFirst()
                            .map(imageFile -> imageFile.getImageFilePath())
                            .orElse("/uploads/member-images/profile.png")
                    : "/uploads/member-images/profile.png";

            StatusDTO status = followService.getStatus(viewerId, member.getId());
            dto.isFollowing = status.isFollowing();
            dto.isFollower = status.isFollower();
            dto.isFollow = status.isFollow();
            dto.hasPendingRequest = hasPendingRequest;
            dto.canViewProfile = status.isCanViewProfile();
            dto.relationStatus = status.getRelationStatus();
            return dto;
        }
    }
}
