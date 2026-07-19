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
    private String profileImage;
    private String content;
    private Long commentLikeCnt;
    private boolean isMine;
    private boolean isLike;
    private String createdTime;
    private String updatedTime;

    public BoardCommentResponseDTO fromEntity(
            Board board,
            BoardComment boardComment,
            boolean isLike,
            Long memberIdx,
            Long commentLikeCnt,
            Member member
    ) {
        BoardCommentResponseDTO dto = new BoardCommentResponseDTO();
        dto.boardIdx = board.getId();
        dto.boardCommentIdx = boardComment.getBoardCommentIdx();
        dto.memberIdx = member.getId();
        dto.name = member.getName();
        dto.nickName = member.getDisplayNickName();
        dto.profileImage = member.getImageFiles().isEmpty()
                ? "/uploads/member-images/profile.png"
                : member.getImageFiles().get(0).getImageFilePath();
        dto.content = boardComment.getContent();
        dto.commentLikeCnt = commentLikeCnt;
        dto.isMine = boardComment.getIsMine(memberIdx);
        dto.isLike = isLike;
        dto.createdTime = boardComment.getCreatedTime();
        dto.updatedTime = boardComment.getUpdatedTime();
        return dto;
    }
}
