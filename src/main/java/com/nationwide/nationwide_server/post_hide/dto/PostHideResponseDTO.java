package com.nationwide.nationwide_server.post_hide.dto;

import com.nationwide.nationwide_server.post_hide.PostHide;
import lombok.Data;

import java.sql.Timestamp;

public class PostHideResponseDTO {

    @Data
    public static class ItemDTO {
        private Long memberIdx;
        private String name;
        private String nickName;
        private String thumbnailProfileImagePath;
        private String hiddenAt;

        public static ItemDTO of(PostHide postHide) {
            ItemDTO dto = new ItemDTO();
            dto.memberIdx = postHide.getViewer().getId();
            dto.name = postHide.getViewer().getName();
            dto.nickName = postHide.getViewer().getDisplayNickName();
            dto.thumbnailProfileImagePath = postHide.getViewer().getImageFiles().stream()
                    .findFirst()
                    .map(imageFile -> imageFile.getImageFilePath())
                    .orElse("/uploads/member-images/profile.png");
            Timestamp createdAt = postHide.getCreatedAt();
            dto.hiddenAt = createdAt == null ? null : createdAt.toInstant().toString();
            return dto;
        }
    }
}
