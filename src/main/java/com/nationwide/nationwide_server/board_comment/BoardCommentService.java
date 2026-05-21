package com.nationwide.nationwide_server.board_comment;

import com.nationwide.nationwide_server._core.errors.exception.Exception401;
import com.nationwide.nationwide_server._core.errors.exception.Exception404;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.alarm.AlarmService;
import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board.BoardRepository;
import com.nationwide.nationwide_server.board.BoardService;
import com.nationwide.nationwide_server.board_comment.dto.BoardCommentRequestDTO;
import com.nationwide.nationwide_server.board_comment.dto.BoardCommentResponseDTO;
import com.nationwide.nationwide_server.board_comment_like.BoardCommentLikeService;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.nationwide.nationwide_server._core._enum.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCommentService {
    private final BoardCommentRepository boardCommentRepository;
    private final BoardCommentLikeService boardCommentLikeService;
    private final MemberService memberService;
    private final AlarmService alarmService;
    private final BoardRepository boardRepository;

    @Transactional
    public void SaveComment(Board board, SessionUser sessionUser, BoardCommentRequestDTO.SaveDTO saveDTO) {
        memberService.validateActiveMember(sessionUser.getId());
        Board managedBoard = boardRepository.findById(board.getId()).orElseThrow(() -> new Exception404(BOARD_NOT_FOUND.getMessage()));
        Member member = memberService.findById(sessionUser.getId());
        BoardComment boardComment = saveDTO.toEntity(managedBoard, member);
        boardCommentRepository.save(boardComment);
        alarmService.createBoardCommentAlarm(member, managedBoard, boardComment);
    }

    @Transactional
    public void updateBoardComment(SessionUser sessionUser, Long commentIdx, BoardCommentRequestDTO.UpdateDTO updateDTO) {
        memberService.validateActiveMember(sessionUser.getId());
        BoardComment boardComment = findByCommentIdx(commentIdx);
        if (!boardComment.getIsMine(sessionUser.getId())) {
            throw new Exception401(COMMENT_NOT_MINE.getMessage());
        }
        boardComment.updateContent(updateDTO.getContent());
    }

    @Transactional
    public void deleteBoardComment(SessionUser sessionUser, Long commentIdx) {
        memberService.validateActiveMember(sessionUser.getId());
        BoardComment boardComment = findByCommentIdx(commentIdx);
        boolean isMine = boardComment.getIsMine(sessionUser.getId());
        if (!isMine) {
            throw new Exception401(COMMENT_NOT_MINE.getMessage());
        }
        boardCommentRepository.deleteByIdSoft(commentIdx);
    }

    public Slice<BoardCommentResponseDTO> commentSlice(Long boardIdx, SessionUser sessionUser) {
        Pageable pageable = Pageable.ofSize(5);
        Slice<BoardComment> boardComments = boardCommentRepository.findCommentSlice(boardIdx, pageable);
        Long loginMemberId = sessionUser.getId();

        return boardComments.map(boardComment -> {
            Long commentLikeCnt = boardCommentLikeService.commentLikeCnt(boardComment.getBoardCommentIdx());
            Board board = boardComment.getBoard();
            Member member = boardComment.getMember();
            boolean isLike = boardCommentLikeService.existsByBoardCommentIdxAndMemberId(
                    boardComment.getBoardCommentIdx(),
                    loginMemberId
            );
            return new BoardCommentResponseDTO().fromEntity(board, boardComment, isLike, sessionUser.getId(), commentLikeCnt, member);
        });
    }

    public Long countCommentByBoardIdx(Long boardIdx) {
        return boardCommentRepository.countCommentByBoardIdx(boardIdx);
    }

    public BoardComment findByCommentIdx(Long commentIdx) {
        return boardCommentRepository.findById(commentIdx)
                .orElseThrow(() -> new Exception404(COMMENT_NOT_FOUND.getMessage()));
    }

    public Slice<BoardComment> findByMemberId(Long memberIdx, Pageable pageable) {
        return boardCommentRepository.findByMemberId(memberIdx, pageable);
    }
}
