package com.nationwide.nationwide_server.board.dto;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_comment.dto.BoardCommentResponseDTO;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import lombok.Data;
import org.springframework.data.domain.Slice;

import java.util.List;

public class BoardResponseDTO {
    @Data
    public static class DetailDTO{
        private Long id;
        private String name;
        private Long memberIdx;
        private String title;
        private String content;
        private Long viewCnt;
        private String createdAt;
        private String updatedAt;
        private Long likeCnt;
        private Long commentCnt;
        private Slice<BoardCommentResponseDTO> commentSlice;
        private boolean isRead; // 읽음 표시
        private boolean isLiked; // 좋아요 표시
        private boolean isMine; // 내가 쓴 글인지
        private List<String> imagePath; // 이미지 경로
        private List<String> imageFileId; // 이미지 파일 아이디

        public static DetailDTO of(SessionUser sessionUser,Board board,Long likeCnt,Long commentCnt, Slice<BoardCommentResponseDTO> commentSlice,List<ImageResponseDTO> imageFiles, boolean isLiked) {
            DetailDTO dto = new DetailDTO();
            dto.id = board.getId();
            dto.name = board.getMember().getName();
            dto.memberIdx = board.getMember().getId();
            dto.title = board.getTitle();
            dto.content = board.getContent();
            dto.viewCnt = board.getViewCnt();
            dto.isMine = board.getIsMine(sessionUser.getId());
            dto.isLiked = isLiked;
            dto.likeCnt = likeCnt;
            dto.commentCnt = commentCnt;
            dto.commentSlice = commentSlice;
            dto.createdAt = board.getCreatedTime();
            dto.updatedAt = board.getUpdatedTime();
            dto.imagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath)
                    .toList();
            dto.imageFileId = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFileId)
                    .toList();
            dto.isMine = sessionUser.getId().equals(board.getMember().getId());
            return dto;
        }
    }

    // 게시물 리스트 반환 SLICE
    @Data
    public static class ListDTO{

        private Long id; // 게시물 번호
        private String name; // 작성자
        private Long memberIdx; // 작성자 번호
        private String title; // 제목
        private String content; // 내용
        private Long viewCnt; // 조회수
        private String createdAt; // 생성일
        private String updatedAt; // 수정일
        private Long likeCnt; // 좋아요 수
        private Long commentCnt; // 댓글 수
        private boolean isRead; // 읽음 표시
        private boolean isLiked; // 좋아요 표시
        private boolean isMine; // 내가 쓴 글인지
        private List<String> imagePath; // 이미지 경로
        private List<String> imageFileId; // 이미지 파일 아이디


        public static ListDTO of(SessionUser sessionUser, Board board,Long likeCnt,Long commentCnt, List<ImageResponseDTO> imageFiles, boolean isLiked){
            ListDTO dto = new ListDTO();
            dto.id = board.getId();
            dto.name = board.getMember().getName();
            dto.memberIdx = board.getMember().getId();
            dto.title = board.getTitle();
            dto.content = board.getContent();
            dto.viewCnt = board.getViewCnt();
            dto.isLiked = isLiked;
            dto.likeCnt = likeCnt;
            dto.commentCnt = commentCnt;
            dto.createdAt = board.getCreatedTime();
            dto.updatedAt = board.getUpdatedTime();
            dto.isMine = sessionUser != null && sessionUser.getId().equals(board.getMember().getId());
            dto.imagePath = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFilePath)
                    .toList();
            dto.imageFileId = imageFiles.stream()
                    .map(ImageResponseDTO::getImageFileId)
                    .toList();
            return dto;
        }
    }
}
