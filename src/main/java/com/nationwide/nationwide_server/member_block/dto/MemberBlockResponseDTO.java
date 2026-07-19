package com.nationwide.nationwide_server.member_block.dto;

import com.nationwide.nationwide_server.member_block.MemberBlock;
import lombok.Data;

import java.sql.Timestamp;

public class MemberBlockResponseDTO {

    @Data
    public static class ItemDTO {
        private Long memberIdx;
        private String name;
        private String nickName;
        private String thumbnailProfileImagePath;
        private String blockedAt;

        public static ItemDTO of(MemberBlock memberBlock) {
            ItemDTO dto = new ItemDTO();
            dto.memberIdx = memberBlock.getBlocked().getId();
            dto.name = memberBlock.getBlocked().getName();
            dto.nickName = memberBlock.getBlocked().getDisplayNickName();
            dto.thumbnailProfileImagePath = memberBlock.getBlocked().getImageFiles().stream()
                    .findFirst()
                    .map(imageFile -> imageFile.getImageFilePath())
                    .orElse("/uploads/member-images/profile.png");
            Timestamp createdAt = memberBlock.getCreatedAt();
            dto.blockedAt = createdAt == null ? null : createdAt.toInstant().toString();
            return dto;
        }
    }
}
