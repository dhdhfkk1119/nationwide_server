package com.nationwide.nationwide_server.board_comment_like.dto;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_comment.BoardComment;
import com.nationwide.nationwide_server.board_comment_like.BoardCommentLike;
import com.nationwide.nationwide_server.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCommentLikeRequestDTO {
    private Board board;
    private BoardComment boardComment;
    private Member member;

    public BoardCommentLike toEntity(Board board,BoardComment boardComment, Member member) {
        return BoardCommentLike.builder()
                .board(board)
                .boardComment(boardComment)
                .member(member).build();
    }
}
