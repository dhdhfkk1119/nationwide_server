package com.nationwide.nationwide_server.post_hide;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post-hides")
@RequiredArgsConstructor
public class PostHideController {
    private final PostHideService postHideService;

    @PostMapping("/{targetMemberId}/toggle")
    public ResponseEntity<?> toggle(
            @LoginUser SessionUser sessionUser,
            @PathVariable("targetMemberId") Long targetMemberId
    ) {
        boolean isHiding = postHideService.toggleHide(sessionUser, targetMemberId);
        return ResponseEntity.ok(ApiUtil.success(isHiding ? "게시물을 숨겼습니다." : "게시물 숨기기를 해제했습니다."));
    }

    @GetMapping
    public ResponseEntity<?> myHiddenList(
            @LoginUser SessionUser sessionUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Slice<?> response = postHideService.hiddenSlice(sessionUser, pageable);
        return ResponseEntity.ok(ApiUtil.success(response));
    }
}
