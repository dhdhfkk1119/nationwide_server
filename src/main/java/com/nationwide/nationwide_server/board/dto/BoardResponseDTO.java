package com.nationwide.nationwide_server.board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_comment.dto.BoardCommentResponseDTO;
import com.nationwide.nationwide_server.follow.dto.FollowResponseDTO;
import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import lombok.Data;
import org.springframework.data.domain.Slice;

import java.util.List;

public class BoardResponseDTO {
    private static String displayName(Board board) {
        return board.getMember().getDisplayNickName();
    }

    @Data
    public static class DetailDTO {
        private Long id;
        private String name;
        private Long memberIdx;
        private String thumbnailProfileImagePath;
        private String title;
        private String content;
        private Long viewCnt;
        private String createdAt;
        private String updatedAt;
        private Long likeCnt;
        private Long commentCnt;
        private Slice<BoardCommentResponseDTO> commentSlice;
        private boolean isRead;
        private boolean isLiked;
        private boolean isMine;
        @JsonProperty("isFollowing")
        private boolean isFollowing;
        @JsonProperty("isFollower")
        private boolean isFollower;
        @JsonProperty("isFollow")
        private boolean isFollow;
        private Double distanceKm;
        private List<String> imagePath;
        private List<String> imageFileId;

        public static DetailDTO of(
                SessionUser sessionUser,
                Board board,
                Long likeCnt,
                Long commentCnt,
                Slice<BoardCommentResponseDTO> commentSlice,
                List<ImageResponseDTO> imageFiles,
                boolean isLiked
        ) {
            DetailDTO dto = new DetailDTO();
            dto.id = board.getId();
            dto.name = displayName(board);
            dto.memberIdx = board.getMember().getId();
            dto.thumbnailProfileImagePath = board.getMember().getImageFiles().stream()
                    .findFirst()
                    .map(imageFile -> imageFile.getImageFilePath())
                    .orElse("/uploads/member-images/profile.png");
            dto.title = board.getTitle();
            dto.content = board.getContent();
            dto.viewCnt = board.getViewCnt();
            dto.isMine = board.getIsMine(sessionUser.getId());
            dto.isLiked = isLiked;
            dto.likeCnt = likeCnt;
            dto.commentCnt = commentCnt;
            dto.commentSlice = commentSlice;
            dto.createdAt = board.getCreatedTime();
            dto.updatedAt = board.getUpdatedTime();
            dto.imagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath)
                    .toList();
            dto.imageFileId = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFileId)
                    .toList();
            dto.isMine = sessionUser.getId().equals(board.getMember().getId());
            return dto;
        }
    }

    @Data
    public static class ListDTO {
        private Long id;
        private String name;
        private Long memberIdx;
        private String thumbnailProfileImagePath;
        private String title;
        private String content;
        private Long viewCnt;
        private String createdAt;
        private String updatedAt;
        private Long likeCnt;
        private Long commentCnt;
        private boolean isRead;
        private boolean isLiked;
        private boolean isMine;
        @JsonProperty("isFollowing")
        private boolean isFollowing;
        @JsonProperty("isFollower")
        private boolean isFollower;
        @JsonProperty("isFollow")
        private boolean isFollow;
        private Double distanceKm;
        private List<String> imagePath;
        private List<String> imageFileId;

        public static ListDTO of(
                SessionUser sessionUser,
                Board board,
                Long likeCnt,
                Long commentCnt,
                List<ImageResponseDTO> imageFiles,
                boolean isLiked,
                FollowResponseDTO.StatusDTO followStatus,
                Double distanceKm
        ) {
            ListDTO dto = new ListDTO();
            dto.id = board.getId();
            dto.name = displayName(board);
            dto.memberIdx = board.getMember().getId();
            dto.thumbnailProfileImagePath = board.getMember().getImageFiles().stream()
                    .findFirst()
                    .map(imageFile -> imageFile.getImageFilePath())
                    .orElse("/uploads/member-images/profile.png");
            dto.title = board.getTitle();
            dto.content = board.getContent();
            dto.viewCnt = board.getViewCnt();
            dto.isLiked = isLiked;
            dto.likeCnt = likeCnt;
            dto.commentCnt = commentCnt;
            dto.createdAt = board.getCreatedTime();
            dto.updatedAt = board.getUpdatedTime();
            dto.isMine = sessionUser != null && sessionUser.getId().equals(board.getMember().getId());
            dto.isFollowing = followStatus != null && followStatus.isFollowing();
            dto.isFollower = followStatus != null && followStatus.isFollower();
            dto.isFollow = followStatus != null && followStatus.isFollow();
            dto.distanceKm = distanceKm;
            dto.imagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath)
                    .toList();
            dto.imageFileId = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFileId)
                    .toList();
            return dto;
        }
    }
}
