package com.nationwide.nationwide_server.board;

import com.nationwide.nationwide_server.board.dto.BoardRequestDTO;
import com.nationwide.nationwide_server.board.dto.BoardResponseDTO;
import com.nationwide.nationwide_server.board_comment_like.BoardCommentLikeRepository;
import com.nationwide.nationwide_server.board_comment_like.BoardCommentLikeService;
import com.nationwide.nationwide_server.board_view.BoardViewService;
import com.nationwide.nationwide_server.board_comment.BoardCommentService;
import com.nationwide.nationwide_server.board_comment.dto.BoardCommentResponseDTO;
import com.nationwide.nationwide_server.board_like.BoardLikeService;
import com.nationwide.nationwide_server.follow.FollowService;
import com.nationwide.nationwide_server.follow.dto.FollowResponseDTO;
import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core._enum.FileDBType;
import com.nationwide.nationwide_server._core.errors.exception.Exception401;
import com.nationwide.nationwide_server._core.errors.exception.Exception404;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.image_file.ImageFile;
import com.nationwide.nationwide_server.image_file.ImageFileService;
import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import com.nationwide.nationwide_server.location.LocationService;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import com.nationwide.nationwide_server.member_block.MemberBlockService;
import com.nationwide.nationwide_server.post_hide.PostHideService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

import static com.nationwide.nationwide_server._core._enum.ErrorCode.BOARD_NOT_FOUND;
import static com.nationwide.nationwide_server._core._enum.ErrorCode.MEMBER_NOT_MINE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberService memberService;
    private final ImageFileService imageFileService;
    private final BoardLikeService boardLikeService;
    private final BoardCommentService boardCommentService;
    private final BoardViewService boardViewService;
    private final FollowService followService;
    private final LocationService locationService;
    private final MemberBlockService memberBlockService;
    private final PostHideService postHideService;

    // 게시물 작성
    @Transactional
    public void saveBoard(SessionUser sessionUser,BoardRequestDTO.SaveDTO saveDTO, List<MultipartFile> files){
        memberService.validateActiveMember(sessionUser.getId());
        Member member = memberService.findById(sessionUser.getId());
        Board board = saveDTO.toEntity(member);

        // 이미지 파일 업로드 및 ImageFile 엔티티 생성
        if(files != null && !files.isEmpty()){
            List<ImageFile> imageFiles = imageFileService.saveImage(files,FileDBType.BOARD);
            for (ImageFile imageFile : imageFiles) {
                board.addImageFile(imageFile);
            }
        }
        boardRepository.save(board);

    }

    // 게시물 상세 정보
    @Transactional
    public BoardResponseDTO.DetailDTO findByBoardId(@LoginUser SessionUser sessionUser, Long boardIdx){
        if (sessionUser == null) {
            throw new Exception401("濡쒓렇?몄씠 ?꾩슂?⑸땲??");
        }

        Board board = findByBoard(boardIdx);
        Member member = memberService.findById(sessionUser.getId());

        if (memberBlockService.isBlockedEitherWay(board.getMember().getId(), member.getId())
                || postHideService.isHiddenFromViewer(board.getMember().getId(), member.getId())) {
            throw new Exception404(BOARD_NOT_FOUND.getMessage());
        }

        boolean shouldIncreaseViewCnt = boardViewService.shouldIncreaseViewCnt(board, member);
        if (shouldIncreaseViewCnt) {
            boardRepository.incrementViewCnt(board.getId());
            board = findByBoard(boardIdx);
        }

        boolean isLike = boardLikeService.existsByBoardIdAndMemberId(board.getId(),member.getId());
        Long likeCnt = boardLikeService.likeCnt(boardIdx);
        Long commentCnt = boardCommentService.countCommentByBoardIdx(boardIdx);
        Slice<BoardCommentResponseDTO> boardCommentSlice = boardCommentService.commentSlice(boardIdx, sessionUser);

        List<ImageResponseDTO> imageFileDTOs = board.getImageFiles().stream()
                .map(imageFile -> imageFileService.imageFileDetailListInfo(imageFile.getImageFileId()))
                .flatMap(List::stream)
                .toList();

        return BoardResponseDTO.DetailDTO.of(sessionUser,board,likeCnt,commentCnt,boardCommentSlice,imageFileDTOs,isLike);
    }
    
    // 게시물 리스트 정보
    public Slice<BoardResponseDTO.ListDTO> findByBoardList(@LoginUser SessionUser sessionUser, Pageable pageable) {
        Long loginMemberId = sessionUser != null ? sessionUser.getId() : null;
        Slice<Board> boards = boardRepository.findSlice(loginMemberId, pageable);
        Member loginMember = loginMemberId != null ? memberService.findById(loginMemberId) : null;

        return boards.map(board -> {
            Long likeCnt = boardLikeService.likeCnt(board.getId());
            boolean isLike = loginMemberId != null &&
                    boardLikeService.existsByBoardIdAndMemberId(board.getId(), loginMemberId);
            Long commentCnt = boardCommentService.countCommentByBoardIdx(board.getId());
            FollowResponseDTO.StatusDTO followStatus = followService.getStatus(loginMemberId, board.getMember().getId());
            List<ImageResponseDTO> imageFileDTOs = board.getImageFiles().stream()
                    .map(imageFile -> new ImageResponseDTO(imageFile)) // 직접 변환 권장
                    .toList();

            Double distanceKm = null;
            if (loginMember != null
                    && !loginMember.getId().equals(board.getMember().getId())
                    && board.getMember().isLocationVisible()) {
                distanceKm = locationService.calculateDistanceKm(
                        loginMember.getLatitude(),
                        loginMember.getLongitude(),
                        board.getMember().getLatitude(),
                        board.getMember().getLongitude()
                );
            }

            return BoardResponseDTO.ListDTO.of(
                    sessionUser,
                    board,
                    likeCnt,
                    commentCnt,
                    imageFileDTOs,
                    isLike,
                    followStatus,
                    distanceKm
            );
        });

    }


    // 게시물 DB 캐쉬 에서 찾기
    public Board findByBoard(Long boardIdx){
        Board board = boardRepository.findById(boardIdx)
                .orElseThrow(() -> new Exception404(BOARD_NOT_FOUND.getMessage()));

        if (memberService.isDeactivated(board.getMember())) {
            throw new Exception404(BOARD_NOT_FOUND.getMessage());
        }

        return board;
    }

    // 게시물 삭제(del_date 업데이트)
    @Transactional
    public void deleteBoard(SessionUser sessionUser, Long boardIdx){
        memberService.validateActiveMember(sessionUser.getId());
        Board board = findByBoard(boardIdx);
        if (!board.getIsMine(sessionUser.getId())) {
            throw new Exception401(MEMBER_NOT_MINE.getMessage());
        }

        int result = boardRepository.deleteByIdSoft(boardIdx);
        if(result == 0) throw new Exception404(BOARD_NOT_FOUND.getMessage());
    }

    // 게시물 업데이트하기 (더티체킹)
    @Transactional
    public void updateBoard(SessionUser sessionUser,BoardRequestDTO.UpdateDTO updateDTO,Long idx,List<MultipartFile> newImages){
        memberService.validateActiveMember(sessionUser.getId());
        Board board = findByBoard(idx);

        if(!sessionUser.getId().equals(board.getMember().getId())){
            throw new Exception401(MEMBER_NOT_MINE.getMessage());
        }

        // 기존 이미지 삭제 및 업데이트
        if(updateDTO.getImageFileIds() != null) {
            imageFileService.syncDeleteImages(board, updateDTO.getImageFileIds());
        }

        // 새 이미지 추가 및 업데이트
        if(newImages != null && !newImages.isEmpty()) {
            imageFileService.addNewImages(board, newImages);
        }

        board.updateBoard(updateDTO);
    }






}
