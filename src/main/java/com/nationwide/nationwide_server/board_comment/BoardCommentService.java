package com.nationwide.nationwide_server.board_comment;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_comment.dto.BoardCommentRequestDTO;
import com.nationwide.nationwide_server.board_comment.dto.BoardCommentResponseDTO;
import com.nationwide.nationwide_server.board_comment_like.BoardCommentLikeService;
import com.nationwide.nationwide_server._core.errors.exception.Exception401;
import com.nationwide.nationwide_server._core.errors.exception.Exception404;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.nationwide.nationwide_server._core._enum.ErrorCode.COMMENT_NOT_FOUND;
import static com.nationwide.nationwide_server._core._enum.ErrorCode.COMMENT_NOT_MINE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCommentService {
    private final BoardCommentRepository boardCommentRepository;
    private final BoardCommentLikeService boardCommentLikeService;
    private final MemberService memberService;
    
    // 댓글 저장하기
    @Transactional
    public void SaveComment(Board board, SessionUser sessionUser,BoardCommentRequestDTO.SaveDTO saveDTO){
        Member member = memberService.findById(sessionUser.getId());
        BoardComment boardComment = saveDTO.toEntity(board,member);
        boardCommentRepository.save(boardComment);
    }

    // 댓글 업데이트
    @Transactional
    public void updateBoardComment(SessionUser sessionUser,Long commentIdx,BoardCommentRequestDTO.UpdateDTO updateDTO){
        BoardComment boardComment = findByCommentIdx(commentIdx);
        if(!boardComment.getIsMine(sessionUser.getId())){
            throw new Exception401(COMMENT_NOT_MINE.getMessage());
        }
        boardComment.updateContent(updateDTO.getContent());
    }
    
    // 댓글 삭제
    @Transactional
    public void deleteBoardComment(SessionUser sessionUser,Long commentIdx){
        BoardComment boardComment = findByCommentIdx(commentIdx);
        boolean isMine = boardComment.getIsMine(sessionUser.getId());
        if(!isMine) throw new Exception401(COMMENT_NOT_MINE.getMessage());
        boardCommentRepository.deleteByIdSoft(commentIdx);
    }

    // 댓글 리스트 목록
    public Slice<BoardCommentResponseDTO> commentSlice(Long boardIdx){
        Pageable pageable = Pageable.ofSize(5);
        Slice<BoardComment> boardComments = boardCommentRepository.findCommentSlice(boardIdx, pageable);

        return boardComments.map(boardComment -> {
            Long commentLikeCnt = boardCommentLikeService.commentLikeCnt(boardComment.getBoardCommentIdx());
            Board board = boardComment.getBoard();
            Member member = boardComment.getMember();
            return new BoardCommentResponseDTO().fromEntity(board,boardComment,commentLikeCnt,member);
        });
    }

    // 게시물에 대한 댓글 갯수
    public Long countCommentByBoardIdx(Long boardIdx) {
        return boardCommentRepository.countCommentByBoardIdx(boardIdx);
    }

    // 댓글 찾기
    public BoardComment findByCommentIdx(Long commentIdx){
        return boardCommentRepository.findById(commentIdx).orElseThrow(() -> new Exception404(COMMENT_NOT_FOUND.getMessage()));
    }


}

