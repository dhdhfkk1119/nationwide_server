package com.nationwide.nationwide_server.my_page;

import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board.BoardRepository;
import com.nationwide.nationwide_server.board.dto.BoardResponseDTO;
import com.nationwide.nationwide_server.board_comment.BoardComment;
import com.nationwide.nationwide_server.board_comment.BoardCommentService;
import com.nationwide.nationwide_server.follow.FollowService;
import com.nationwide.nationwide_server.follow.dto.FollowResponseDTO;
import com.nationwide.nationwide_server.board_like.BoardLikeService;
import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import com.nationwide.nationwide_server.my_page.dto.MyPageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {
    private final BoardRepository boardRepository;
    private final BoardLikeService boardLikeService;
    private final BoardCommentService boardCommentService;
    private final MemberService memberService;
    private final FollowService followService;

    public MyPageResponseDTO.SummaryDTO summary(SessionUser sessionUser) {
        Member member = memberService.findById(sessionUser.getId());
        return buildSummary(member, sessionUser != null ? sessionUser.getId() : null);
    }

    public MyPageResponseDTO.SummaryDTO memberSummary(SessionUser sessionUser, Long memberId) {
        Member member = memberService.findById(memberId);
        return buildSummary(member, sessionUser != null ? sessionUser.getId() : null);
    }

    public Slice<BoardResponseDTO.ListDTO> memberBoardSlice(
            SessionUser sessionUser,
            Long memberId,
            Pageable pageable
    ) {
        Member member = memberService.findById(memberId);
        Long viewerId = sessionUser != null ? sessionUser.getId() : null;
        if (!memberService.canExposeMember(member, viewerId) || !followService.canViewProfile(viewerId, member)) {
            return new SliceImpl<>(List.of(), pageable, false);
        }
        Slice<Board> boardSlice = boardRepository.findByMemberId(memberId, pageable);
        return mapBoardSlice(sessionUser, boardSlice);
    }

    private MyPageResponseDTO.SummaryDTO buildSummary(Member member, Long viewerId) {
        List<ImageResponseDTO> imageFileDTOs = member.getImageFiles().stream()
                .map(ImageResponseDTO::new)
                .toList();
        Long boardCnt = boardRepository.countByMemberId(member.getId());
        FollowResponseDTO.StatusDTO status = followService.getStatus(viewerId, member.getId());
        boolean canExposeMember = memberService.canExposeMember(member, viewerId);

        return MyPageResponseDTO.SummaryDTO.of(
                member,
                imageFileDTOs,
                boardCnt,
                status.getFollowerCnt(),
                status.getFollowingCnt(),
                member.isPrivateProfile(),
                member.isLocationVisible(),
                status.isCanViewProfile(),
                status.isHasPendingRequest(),
                status.getRelationStatus(),
                status.isFollowing(),
                status.isFollower(),
                canExposeMember
        );
    }

    public Slice<BoardResponseDTO.ListDTO> boardSlice(SessionUser sessionUser, Pageable pageable) {
        Slice<Board> boardSlice = boardRepository.findByMemberId(sessionUser.getId(), pageable);
        return mapBoardSlice(sessionUser, boardSlice);
    }

    public Slice<BoardResponseDTO.ListDTO> favoriteBoardSlice(SessionUser sessionUser, Pageable pageable) {
        Slice<Board> boardSlice = boardRepository.findFavoriteBoardSlice(sessionUser.getId(), pageable);
        return mapBoardSlice(sessionUser, boardSlice);
    }

    public Slice<MyPageResponseDTO.CommentListDTO> commentSlice(SessionUser sessionUser, Pageable pageable) {
        Slice<BoardComment> commentSlice = boardCommentService.findByMemberId(sessionUser.getId(), pageable);
        return commentSlice.map(boardComment -> {
            Board board = boardComment.getBoard();
            Long likeCnt = boardLikeService.likeCnt(board.getId());
            Long commentCnt = boardCommentService.countCommentByBoardIdx(board.getId());
            boolean isLike = boardLikeService.existsByBoardIdAndMemberId(board.getId(), sessionUser.getId());

            return MyPageResponseDTO.CommentListDTO.of(
                    boardComment,
                    board,
                    likeCnt,
                    commentCnt,
                    isLike
            );
        });
    }

    private Slice<BoardResponseDTO.ListDTO> mapBoardSlice(SessionUser sessionUser, Slice<Board> boardSlice) {
        Long loginMemberId = sessionUser != null ? sessionUser.getId() : null;

        return boardSlice.map(board -> {
            Long likeCnt = boardLikeService.likeCnt(board.getId());
            boolean isLike = loginMemberId != null &&
                    boardLikeService.existsByBoardIdAndMemberId(board.getId(), loginMemberId);
            Long commentCnt = boardCommentService.countCommentByBoardIdx(board.getId());
            FollowResponseDTO.StatusDTO followStatus = followService.getStatus(loginMemberId, board.getMember().getId());
            List<ImageResponseDTO> imageFileDTOs = board.getImageFiles().stream()
                    .map(ImageResponseDTO::new)
                    .toList();

            return BoardResponseDTO.ListDTO.of(sessionUser, board, likeCnt, commentCnt, imageFileDTOs, isLike, followStatus);
        });
    }
}
