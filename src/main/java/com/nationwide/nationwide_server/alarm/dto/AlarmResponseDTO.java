package com.nationwide.nationwide_server.alarm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nationwide.nationwide_server.alarm.Alarm;
import com.nationwide.nationwide_server.alarm.AlarmType;
import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.image_file.ImageFile;
import com.nationwide.nationwide_server.member.Member;
import lombok.Data;

public class AlarmResponseDTO {

    private static String actorName(Member actor) {
        return actor.getDisplayNickName();
    }

    private static String actorThumbnail(Member actor) {
        return actor.getImageFiles().stream()
                .findFirst()
                .map(ImageFile::getImageFilePath)
                .orElse("/uploads/member-images/profile.png");
    }

    private static String boardThumbnail(Board board) {
        if (board == null) {
            return null;
        }
        return board.getImageFiles().stream()
                .findFirst()
                .map(ImageFile::getImageFilePath)
                .orElse(null);
    }

    private static String message(Alarm alarm) {
        String actorName = actorName(alarm.getActor());
        AlarmType type = alarm.getType();

        return switch (type) {
            case FOLLOW -> actorName + " 사용자가 회원님을 팔로우했습니다.";
            case FOLLOW_REQUEST_REJECTED -> actorName + " 사용자가 비공개 게시물 공개 요청을 거절했습니다.";
            case BOARD_LIKE -> actorName + " 사용자가 회원님의 스토리에 좋아요를 눌렀습니다.";
            case BOARD_COMMENT -> actorName + " 사용자가 회원님의 스토리에 댓글을 남겼습니다.";
            case BOARD_COMMENT_LIKE -> actorName + " 사용자가 회원님의 스토리 댓글에 좋아요를 눌렀습니다.";
        };
    }

    @Data
    public static class UnreadCountDTO {
        private Long unreadCount;

        public UnreadCountDTO(Long unreadCount) {
            this.unreadCount = unreadCount;
        }
    }

    @Data
    public static class StreamDTO {
        private Long unreadCount;
        private ListDTO alarm;

        public StreamDTO(Long unreadCount, ListDTO alarm) {
            this.unreadCount = unreadCount;
            this.alarm = alarm;
        }
    }

    @Data
    public static class ListDTO {
        private Long alarmIdx;
        private String type;
        private String message;
        private String createdAt;
        @JsonProperty("isRead")
        private boolean isRead;
        private Long actorMemberIdx;
        private String actorName;
        private String actorThumbnailProfileImagePath;
        private Long boardIdx;
        private String boardThumbnailImagePath;

        public static ListDTO of(Alarm alarm) {
            ListDTO dto = new ListDTO();
            dto.alarmIdx = alarm.getAlarmIdx();
            dto.type = alarm.getType().name();
            dto.message = message(alarm);
            dto.createdAt = alarm.getCreatedTime();
            dto.isRead = alarm.isRead();
            dto.actorMemberIdx = alarm.getActor().getId();
            dto.actorName = actorName(alarm.getActor());
            dto.actorThumbnailProfileImagePath = actorThumbnail(alarm.getActor());
            dto.boardIdx = alarm.getBoard() != null ? alarm.getBoard().getId() : null;
            dto.boardThumbnailImagePath = boardThumbnail(alarm.getBoard());
            return dto;
        }
    }
}
