package com.nationwide.nationwide_server.board_comment_like;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board.BoardService;
import com.nationwide.nationwide_server.board_comment.BoardComment;
import com.nationwide.nationwide_server.board_comment.BoardCommentService;
import com.nationwide.nationwide_server.board_comment.dto.BoardCommentRequestDTO;
import com.nationwide.nationwide_server.board_comment_like.dto.BoardCommentLikeRequestDTO;
import com.nationwide.nationwide_server.board_like.dto.BoardLikeRequestDTO;
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
    private final BoardCommentService boardCommentService;
    private final MemberService memberService;
    private final BoardService boardService;

    // 좋아요 수
    public Long commentLikeCnt(Long boardCommentIdx){
        return boardCommentLikeRepository.countByBoardCommentId(boardCommentIdx);
    }
    
    // 내가 댓글에 좋아요 눌렀는지 체크 용
    public Boolean isCommentLike(Long boardCommentIdx, Long memberIdx){
        return boardCommentLikeRepository.existsByBoardCommentIdAndMemberId(boardCommentIdx,memberIdx);
    }

    public void toggleCommentLike(Long boardIdx, Long boardCommentIdx, Long memberIdx){
        BoardCommentLike boardCommentLike = boardCommentLikeRepository.findByBoardIdAndMemberId(boardCommentIdx,memberIdx);
        BoardComment boardComment = boardCommentService.findByCommentIdx(boardCommentIdx);
        Member member = memberService.findById(memberIdx);
        Board board = boardService.findByBoard(boardIdx);
        if(boardCommentLike == null){
            BoardCommentLikeRequestDTO dto = new BoardCommentLikeRequestDTO();
            boardCommentLikeRepository.save(dto.toEntity(board,boardComment,member));
        }else {
            boardCommentLikeRepository.delete(boardCommentLike);
        }
    }
}
