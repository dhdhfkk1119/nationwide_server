package com.nationwide.nationwide_server.board_comment;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core._enum.ResourceType;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board.BoardService;
import com.nationwide.nationwide_server.board_comment.dto.BoardCommentRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/board-comments")
@RequiredArgsConstructor
public class BoardCommentController {

    private final BoardCommentService boardCommentService;
    private final BoardService boardService;

    @PostMapping("/{boardId}")
    public ResponseEntity<?> saveComment(
            @PathVariable("boardId") Long boardId,
            @LoginUser SessionUser sessionUser,
            @RequestBody BoardCommentRequestDTO.SaveDTO saveDTO
    ) {
        Board board = boardService.findByBoard(boardId);
        boardCommentService.SaveComment(board, sessionUser, saveDTO);
        return ResponseEntity.ok(ApiUtil.success(ResourceType.COMMENT.getSaveSuccess()));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable("commentId") Long commentId,
            @LoginUser SessionUser sessionUser,
            @RequestBody BoardCommentRequestDTO.UpdateDTO updateDTO
    ) {
        boardCommentService.updateBoardComment(sessionUser, commentId, updateDTO);
        return ResponseEntity.ok(ApiUtil.success(ResourceType.COMMENT.getUpdateSuccess()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("commentId") Long commentId,
            @LoginUser SessionUser sessionUser
    ) {
        boardCommentService.deleteBoardComment(sessionUser, commentId);
        return ResponseEntity.ok(ApiUtil.success(ResourceType.COMMENT.getDeleteSuccess()));
    }
}
