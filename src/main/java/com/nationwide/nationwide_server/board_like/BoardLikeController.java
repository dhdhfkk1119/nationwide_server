package com.nationwide.nationwide_server.board_like;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/board-likes")
@RequiredArgsConstructor
public class BoardLikeController {
    private final BoardLikeService boardLikeService;

    @PostMapping("/{boardId}/toggle")
    public ResponseEntity<?> likeToggle(
            @LoginUser SessionUser sessionUser,
            @PathVariable("boardId") Long boardIdx
    ){
        String alert = boardLikeService.toggleBoardLike(sessionUser, boardIdx);
        return ResponseEntity.ok(ApiUtil.success(alert));
    }
}
