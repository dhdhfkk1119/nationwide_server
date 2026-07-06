package com.nationwide.nationwide_server.board_comment_like;

import com.nationwide.nationwide_server._core._enum.ErrorCode;
import com.nationwide.nationwide_server._core._enum.ResourceType;
import com.nationwide.nationwide_server._core.errors.exception.Exception404;
import com.nationwide.nationwide_server.alarm.AlarmService;
import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_comment.BoardComment;
import com.nationwide.nationwide_server.board_comment.BoardCommentRepository;
import com.nationwide.nationwide_server.board_comment_like.dto.BoardCommentLikeRequestDTO;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BoardCommentLikeService {
    private final BoardCommentLikeRepository boardCommentLikeRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final MemberService memberService;
    private final AlarmService alarmService;

    // 좋아요 수
    public Long commentLikeCnt(Long boardCommentIdx){
        return boardCommentLikeRepository.countByBoardCommentId(boardCommentIdx);
    }
    
    // 내가 댓글에 좋아요 눌렀는지 체크 용
    public Boolean existsByBoardCommentIdxAndMemberId(Long boardCommentIdx, Long memberIdx){
        return boardCommentLikeRepository.existsByBoardCommentIdAndMemberId(boardCommentIdx,memberIdx);
    }

    @Transactional
    public String toggleCommentLike(Long boardCommentIdx, Long memberIdx){
        memberService.validateActiveMember(memberIdx);
        BoardComment boardComment = boardCommentRepository.findById(boardCommentIdx).orElseThrow(() -> new Exception404(ErrorCode.COMMENT_NOT_FOUND.getMessage()));
        Member member = memberService.findById(memberIdx);
        Board board = boardComment.getBoard();
        BoardCommentLike boardCommentLike = boardCommentLikeRepository.findByBoardCommentIdAndMemberId(boardCommentIdx,memberIdx);
        if(boardCommentLike == null){
            BoardCommentLikeRequestDTO dto = new BoardCommentLikeRequestDTO();
            boardCommentLikeRepository.save(dto.toEntity(board,boardComment,member));
            alarmService.createBoardCommentLikeAlarm(member, boardComment);
        }else {
            boardCommentLikeRepository.delete(boardCommentLike);
        }
        return ResourceType.COMMENT.getToggleLike(boardCommentLike != null);
    }
}
