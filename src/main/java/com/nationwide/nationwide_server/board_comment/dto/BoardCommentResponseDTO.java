package com.nationwide.nationwide_server.board_comment.dto;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_comment.BoardComment;
import com.nationwide.nationwide_server.member.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardCommentResponseDTO {

    private Long boardIdx;
    private Long boardCommentIdx;
    private Long memberIdx;
    private String name;
    private String nickName;
    private String content;
    private Long commentLikeCnt;
    private boolean isMine;
    private String createdTime;
    private String updatedTime;

    public BoardCommentResponseDTO fromEntity(Board board, BoardComment boardComment,Long commentLikeCnt, Member member){
        return new BoardCommentResponseDTO(
                this.boardIdx = board.getId(),
                this.boardCommentIdx = boardComment.getBoardCommentIdx(),
                this.memberIdx = member.getId(),
                this.name = member.getName(),
                this.nickName = member.getNickName(),
                this.content = boardComment.getContent(),
                this.commentLikeCnt = commentLikeCnt,
                this.isMine = boardComment.getIsMine(member.getId()),
                this.createdTime = boardComment.getCreatedTime(),
                this.updatedTime = boardComment.getUpdatedTime()
        );
    }
}
