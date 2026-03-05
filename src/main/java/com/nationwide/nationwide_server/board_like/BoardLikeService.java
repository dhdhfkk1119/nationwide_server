package com.nationwide.nationwide_server.board_like;

import com.nationwide.nationwide_server._core._enum.ResourceType;
import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board.BoardService;
import com.nationwide.nationwide_server.board_like.dto.BoardLikeRequestDTO;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardLikeService {

    private final BoardLikeRepository boardLikeRepository;
    private final BoardService boardService;
    private final MemberService memberService;

    @Transactional
    public String toggleBoardLike(SessionUser sessionUser, Long boardIdx){
        Board board = boardService.findByBoard(boardIdx);
        Member member = memberService.findById(sessionUser.getId());

        // 게시판 좋아요 있는 체크 있으면 -> 삭제
        BoardLike boardLike = boardLikeRepository.findByBoardIdAndMemberId(boardIdx,sessionUser.getId());

        if(boardLike == null){
            BoardLikeRequestDTO dto = new BoardLikeRequestDTO();
            boardLikeRepository.save(dto.toEntity(board,member));
        }else {
            boardLikeRepository.delete(boardLike);

        }
        return ResourceType.COMMENT.getToggleLike(boardLike != null);
    }

    public boolean existsByBoardIdAndMemberId(Long boardIdx, Long memberIdx){
        // 내가 게시판을 좋아요 눌렀는지 확인 boolean 확인용
        return boardLikeRepository.existsByBoardIdAndMemberId(boardIdx,memberIdx);
    }

    // 좋아요 갯수
    public Long likeCnt(Long boardIdx){
        return boardLikeRepository.countByBoardId(boardIdx);
    }


}
