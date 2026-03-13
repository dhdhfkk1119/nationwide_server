package com.nationwide.nationwide_server.board_comment_like;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/board-comment-likes")
@RequiredArgsConstructor
public class BoardCommentLikeController {

    private final BoardCommentLikeService boardCommentLikeService;

    @PostMapping("/{commentId}/toggle")
    public ResponseEntity<?> likeToggle(@PathVariable("commentId") Long commentId,
                                       @LoginUser SessionUser sessionUser ){
        String alert = boardCommentLikeService.toggleCommentLike(commentId, sessionUser.getId());
        return ResponseEntity.ok(ApiUtil.success(alert));
    }
}
