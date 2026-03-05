package com.nationwide.nationwide_server.board_like;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core._enum.ResourceType;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/board-likes")
@RequiredArgsConstructor
public class BoardLikeController {
    private final BoardLikeService boardLikeService;

    public ResponseEntity<?> likeToggle(@LoginUser SessionUser sessionUser, Long boardIdx){
        String alert = boardLikeService.toggleBoardLike(sessionUser,boardIdx);
        return ResponseEntity.ok(ApiUtil.success(alert));
    }
}
