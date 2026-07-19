package com.nationwide.nationwide_server.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.message.Message;
import com.nationwide.nationwide_server.message.MessageThread;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

public class MessageResponseDTO {
    private static String formatTimestamp(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().toString();
    }

    private static String thumbnailOf(Member member) {
        return member.getImageFiles().stream()
                .findFirst()
                .map(imageFile -> imageFile.getImageFilePath())
                .orElse("/uploads/member-images/profile.png");
    }

    @Data
    public static class ThreadDetailDTO {
        private Long threadId;
        private Long memberIdx;
        private String name;
        private String nickName;
        private String thumbnailProfileImagePath;

        public static ThreadDetailDTO of(MessageThread thread, Long viewerId) {
            Member other = thread.other(viewerId);
            ThreadDetailDTO dto = new ThreadDetailDTO();
            dto.threadId = thread.getId();
            dto.memberIdx = other.getId();
            dto.name = other.getName();
            dto.nickName = other.getDisplayNickName();
            dto.thumbnailProfileImagePath = thumbnailOf(other);
            return dto;
        }
    }

    @Data
    public static class ThreadListItemDTO {
        private Long threadId;
        private Long memberIdx;
        private String name;
        private String nickName;
        private String thumbnailProfileImagePath;
        private String lastMessagePreview;
        private String lastMessageAt;
        private long unreadCount;

        public static ThreadListItemDTO of(MessageThread thread, Long viewerId, long unreadCount) {
            Member other = thread.other(viewerId);
            ThreadListItemDTO dto = new ThreadListItemDTO();
            dto.threadId = thread.getId();
            dto.memberIdx = other.getId();
            dto.name = other.getName();
            dto.nickName = other.getDisplayNickName();
            dto.thumbnailProfileImagePath = thumbnailOf(other);
            dto.lastMessagePreview = thread.getLastMessagePreview();
            dto.lastMessageAt = formatTimestamp(thread.getLastMessageAt());
            dto.unreadCount = unreadCount;
            return dto;
        }
    }

    @Data
    public static class ThreadListDTO {
        private List<ThreadListItemDTO> content;
        private boolean hasNext;

        public static ThreadListDTO of(List<ThreadListItemDTO> content, boolean hasNext) {
            ThreadListDTO dto = new ThreadListDTO();
            dto.content = content;
            dto.hasNext = hasNext;
            return dto;
        }
    }

    @Data
    public static class MessageItemDTO {
        private Long messageId;
        private Long threadId;
        private Long senderId;
        private String content;
        private String createdAt;
        @JsonProperty("isMine")
        private boolean isMine;

        public static MessageItemDTO of(Message message, Long viewerId) {
            MessageItemDTO dto = new MessageItemDTO();
            dto.messageId = message.getId();
            dto.threadId = message.getThread().getId();
            dto.senderId = message.getSender().getId();
            dto.content = message.getContent();
            dto.createdAt = formatTimestamp(message.getCreatedAt());
            dto.isMine = message.getSender().getId().equals(viewerId);
            return dto;
        }
    }

    @Data
    public static class MessageListDTO {
        private List<MessageItemDTO> content;
        private boolean hasNext;

        public static MessageListDTO of(List<MessageItemDTO> content, boolean hasNext) {
            MessageListDTO dto = new MessageListDTO();
            dto.content = content;
            dto.hasNext = hasNext;
            return dto;
        }
    }
}
