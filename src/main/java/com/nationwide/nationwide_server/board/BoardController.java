package com.nationwide.nationwide_server.board;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core._enum.ResourceType;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.board.dto.BoardRequestDTO;
import com.nationwide.nationwide_server.board.dto.BoardResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/boards")
@Slf4j
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/detail/{boardId}")
    public ResponseEntity<?> findByBoard(
            @PathVariable("boardId") Long boardId,
            @LoginUser SessionUser sessionUser
    ) {
        log.info("boardId 데이터 찍힘 확인={}", boardId);
        BoardResponseDTO.DetailDTO dto = boardService.findByBoardId(sessionUser, boardId);
        return ResponseEntity.ok(ApiUtil.success(dto));
    }

    @GetMapping("/list")
    public ResponseEntity<?> findByBoardList(
            @LoginUser SessionUser sessionUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Slice<BoardResponseDTO.ListDTO> dto = boardService.findByBoardList(sessionUser, pageable);
        return ResponseEntity.ok(ApiUtil.success(dto));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveBoard(
            @LoginUser SessionUser sessionUser,
            @RequestPart("data") BoardRequestDTO.SaveDTO saveDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        boardService.saveBoard(sessionUser, saveDTO, files);
        return ResponseEntity.ok(ApiUtil.success(ResourceType.BOARD.getSaveSuccess()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBoard(@PathVariable("id") Long boardIdx) {
        boardService.deleteBoard(boardIdx);
        return ResponseEntity.ok(ApiUtil.success(ResourceType.BOARD.getDeleteSuccess()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateBoard(
            @PathVariable("id") Long boardIdx,
            @LoginUser SessionUser sessionUser,
            @RequestPart("data") BoardRequestDTO.UpdateDTO updateDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        boardService.updateBoard(sessionUser, updateDTO, boardIdx, files);
        return ResponseEntity.ok(ApiUtil.success(ResourceType.BOARD.getUpdateSuccess()));
    }
}
