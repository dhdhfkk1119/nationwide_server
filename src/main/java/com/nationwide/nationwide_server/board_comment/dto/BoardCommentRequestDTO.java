package com.nationwide.nationwide_server.board_comment.dto;


import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_comment.BoardComment;
import com.nationwide.nationwide_server.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public class BoardCommentRequestDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveDTO {
        private String content;
        private Board board;
        private Member member;

        public BoardComment toEntity(Board board, Member member) {
            return BoardComment.builder()
                    .content(content)
                    .board(board)
                    .member(member)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO {
        private String content;
    }

}
