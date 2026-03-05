package com.nationwide.nationwide_server.board_like.dto;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_like.BoardLike;
import com.nationwide.nationwide_server.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardLikeRequestDTO {
    private Board board;
    private Member member;

    public BoardLike toEntity(Board board, Member member) {
       return BoardLike.builder()
               .board(board)
               .member(member)
               .build();
    }
}
