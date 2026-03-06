package com.nationwide.nationwide_server.board;

import com.nationwide.nationwide_server.board.dto.BoardRequestDTO;
import com.nationwide.nationwide_server.board.dto.BoardResponseDTO;
import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core._enum.ResourceType;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    // 상세 보기
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findByBoard(@PathVariable("id")Long boardIdx, SessionUser sessionUser){
        BoardResponseDTO.DetailDTO dto = boardService.findByBoardId(sessionUser,boardIdx);
        return ResponseEntity.ok(ApiUtil.success(dto));
    }

    // 게시물 리스트 무한 스크롤
    @GetMapping("/list")
    public ResponseEntity<?> findByBoardList(@LoginUser SessionUser sessionUser, @PageableDefault(size = 10) Pageable pageable){
        Slice<BoardResponseDTO.ListDTO> dto = boardService.findByBoardList(sessionUser,pageable);
        return ResponseEntity.ok(ApiUtil.success(dto));
    }

    // 게시물 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveBoard(
            @LoginUser SessionUser sessionUser,
            @RequestPart("data") BoardRequestDTO.SaveDTO saveDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        boardService.saveBoard(sessionUser,saveDTO, files);
        return ResponseEntity.ok(ApiUtil.success(ResourceType.BOARD.getSaveSuccess()));
    }

    // 게시물 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBoard(@PathVariable("id") Long boardIdx){
        boardService.deleteBoard(boardIdx);
        return ResponseEntity.ok(ApiUtil.success(ResourceType.BOARD.getDeleteSuccess()));
    }
    
    // 게시물 수정
    @PutMapping(value = "/{id}" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateBoard(@PathVariable("id") Long boardIdx,SessionUser sessionUser,
                                         @RequestPart("data") BoardRequestDTO.UpdateDTO updateDTO,
                                         @RequestPart(value = "files", required = false) List<MultipartFile> files){
        boardService.updateBoard(sessionUser,updateDTO,boardIdx,files);
        return ResponseEntity.ok(ApiUtil.success(ResourceType.BOARD.getUpdateSuccess()));
    }
}
