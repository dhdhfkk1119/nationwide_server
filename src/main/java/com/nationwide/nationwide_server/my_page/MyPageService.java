package com.nationwide.nationwide_server.my_page;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board.BoardRepository;
import com.nationwide.nationwide_server.board.BoardService;
import com.nationwide.nationwide_server.board.dto.BoardResponseDTO;
import com.nationwide.nationwide_server.board_comment.BoardCommentService;
import com.nationwide.nationwide_server.board_like.BoardLikeService;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final BoardService boardService;
    private final BoardRepository boardRepository;
    private final BoardLikeService boardLikeService;
    private final BoardCommentService boardCommentService;
    private final MemberService memberService;


    // 내가 찜했던 게시판 목록
    public Slice<BoardResponseDTO.ListDTO> favoriteBoardSlice(SessionUser sessionUser, Pageable pageable){
        Slice<Board> boardSlice = boardRepository.findFavoriteBoardSlice(sessionUser.getId(),pageable);
        Member member = memberService.findById(sessionUser.getId());

        return boardSlice.map(board -> {
            Long likeCnt = boardLikeService.likeCnt(board.getId());
            boolean isLike = boardLikeService.existsByBoardIdAndMemberId(board.getId(),member.getId());
            Long commentCnt = boardCommentService.countCommentByBoardIdx(board.getId());
            List<ImageResponseDTO> imageFileDTOs = board.getImageFiles().stream()
                    .map(imageFile -> new ImageResponseDTO(imageFile)) // 직접 변환 권장
                    .toList();


            return BoardResponseDTO.ListDTO.of(sessionUser, board,likeCnt,commentCnt, imageFileDTOs,isLike);
        });
    }
}
