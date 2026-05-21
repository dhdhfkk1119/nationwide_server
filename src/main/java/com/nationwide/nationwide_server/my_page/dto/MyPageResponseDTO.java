package com.nationwide.nationwide_server.my_page.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_comment.BoardComment;
import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import com.nationwide.nationwide_server.member.Member;
import lombok.Data;

import java.util.List;

public class MyPageResponseDTO {
    private static final String DEACTIVATED_MEMBER_NAME = "비활성화된 계정";

    @Data
    public static class SummaryDTO {
        private Long memberIdx;
        private String name;
        private String nickName;
        private String bio;
        private String thumbnailProfileImagePath;
        private List<String> profileImagePath;
        private Long boardCnt;
        private Long followerCnt;
        private Long followingCnt;
        private boolean isPrivateProfile;
        private boolean isLocationVisible;
        private boolean canViewProfile;
        private boolean hasPendingRequest;
        private String relationStatus;
        @JsonProperty("isFollowing")
        private boolean isFollowing;
        @JsonProperty("isFollower")
        private boolean isFollower;
        @JsonProperty("isFollow")
        private boolean isFollow;

        public static SummaryDTO of(
                Member member,
                List<ImageResponseDTO> imageFiles,
                Long boardCnt,
                Long followerCnt,
                Long followingCnt,
                boolean isPrivateProfile,
                boolean isLocationVisible,
                boolean canViewProfile,
                boolean hasPendingRequest,
                String relationStatus,
                boolean isFollowing,
                boolean isFollower,
                boolean canExposeMember
        ) {
            SummaryDTO dto = new SummaryDTO();
            dto.memberIdx = member.getId();
            dto.name = canExposeMember ? member.getName() : DEACTIVATED_MEMBER_NAME;
            dto.nickName = canExposeMember ? member.getNickName() : null;
            dto.bio = canExposeMember ? member.getBio() : null;
            dto.thumbnailProfileImagePath = canExposeMember
                    ? imageFiles.stream()
                            .map(ImageResponseDTO::getImageFilePath)
                            .findFirst()
                            .orElse("/uploads/member-images/profile.png")
                    : "/uploads/member-images/profile.png";
            dto.profileImagePath = canExposeMember
                    ? imageFiles.stream()
                            .map(ImageResponseDTO::getImageFilePath)
                            .toList()
                    : List.of("/uploads/member-images/profile.png");
            dto.boardCnt = canExposeMember ? boardCnt : 0L;
            dto.followerCnt = followerCnt;
            dto.followingCnt = followingCnt;
            dto.isPrivateProfile = isPrivateProfile;
            dto.isLocationVisible = isLocationVisible;
            dto.canViewProfile = canViewProfile && canExposeMember;
            dto.hasPendingRequest = hasPendingRequest;
            dto.relationStatus = relationStatus;
            dto.isFollowing = isFollowing;
            dto.isFollower = isFollower;
            dto.isFollow = isFollowing && isFollower;
            return dto;
        }
    }

    @Data
    public static class CommentListDTO {
        private Long boardIdx;
        private Long boardCommentIdx;
        private String commentContent;
        private String commentCreatedAt;
        private String boardTitle;
        private String boardContent;
        private String boardAuthorName;
        private String boardAuthorThumbnailProfileImagePath;
        private Long boardLikeCnt;
        private Long boardCommentCnt;
        private Long boardViewCnt;
        private boolean boardIsLiked;
        private List<String> imagePath;

        public static CommentListDTO of(
                BoardComment boardComment,
                Board board,
                Long boardLikeCnt,
                Long boardCommentCnt,
                boolean boardIsLiked
        ) {
            CommentListDTO dto = new CommentListDTO();
            dto.boardIdx = board.getId();
            dto.boardCommentIdx = boardComment.getBoardCommentIdx();
            dto.commentContent = boardComment.getContent();
            dto.commentCreatedAt = boardComment.getCreatedTime();
            dto.boardTitle = board.getTitle();
            dto.boardContent = board.getContent();
            dto.boardAuthorName = board.getMember().getNickName() != null && !board.getMember().getNickName().isBlank()
                    ? board.getMember().getNickName()
                    : board.getMember().getName();
            dto.boardAuthorThumbnailProfileImagePath = board.getMember().getImageFiles().stream()
                    .findFirst()
                    .map(imageFile -> imageFile.getImageFilePath())
                    .orElse("/uploads/member-images/profile.png");
            dto.boardLikeCnt = boardLikeCnt;
            dto.boardCommentCnt = boardCommentCnt;
            dto.boardViewCnt = board.getViewCnt();
            dto.boardIsLiked = boardIsLiked;
            dto.imagePath = board.getImageFiles().stream()
                    .map(imageFile -> imageFile.getImageFilePath())
                    .toList();
            return dto;
        }
    }
}
