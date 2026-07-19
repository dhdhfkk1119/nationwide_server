package com.nationwide.nationwide_server.report.dto;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_comment.BoardComment;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.report.Report;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

public class ReportResponseDTO {
    private static String formatTimestamp(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().toString();
    }

    @Data
    public static class ItemDTO {
        private Long reportId;
        private String targetType;
        private Long targetId;
        private String status;
        private String reporterComment;
        private String adminComment;
        private String createdAt;
        private String processedAt;
        private String targetContent;
        private Long boardId;
        private String targetImagePath;

        public static ItemDTO ofBoard(Report report, Board board) {
            ItemDTO dto = common(report);
            dto.targetContent = board.getContent();
            dto.boardId = board.getId();
            dto.targetImagePath = board.getImageFiles().stream()
                    .findFirst()
                    .map(image -> image.getImageFilePath())
                    .orElse(null);
            return dto;
        }

        public static ItemDTO ofMember(Report report, Member member) {
            ItemDTO dto = common(report);
            dto.targetContent = member.getDisplayNickName();
            dto.targetImagePath = member.getImageFiles().stream()
                    .findFirst()
                    .map(image -> image.getImageFilePath())
                    .orElse(null);
            return dto;
        }

        public static ItemDTO ofComment(Report report, BoardComment comment) {
            ItemDTO dto = common(report);
            dto.targetContent = comment.getContent();
            dto.boardId = comment.getBoard().getId();
            dto.targetImagePath = comment.getBoard().getImageFiles().stream()
                    .findFirst()
                    .map(image -> image.getImageFilePath())
                    .orElse(null);
            return dto;
        }

        private static ItemDTO common(Report report) {
            ItemDTO dto = new ItemDTO();
            dto.reportId = report.getId();
            dto.targetType = report.getTargetType().name();
            dto.targetId = report.getTargetId();
            dto.status = report.getStatus().name();
            dto.reporterComment = report.getReporterComment();
            dto.adminComment = report.getAdminComment();
            dto.createdAt = formatTimestamp(report.getCreatedAt());
            dto.processedAt = formatTimestamp(report.getProcessedAt());
            return dto;
        }
    }

    @Data
    public static class ReportListDTO {
        private List<ItemDTO> items;

        public static ReportListDTO of(List<ItemDTO> items) {
            ReportListDTO dto = new ReportListDTO();
            dto.items = items;
            return dto;
        }
    }
}
