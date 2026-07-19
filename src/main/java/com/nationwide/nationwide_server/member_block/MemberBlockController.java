package com.nationwide.nationwide_server.member_block;

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
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class MemberBlockController {
    private final MemberBlockService memberBlockService;

    @PostMapping("/{targetMemberId}/toggle")
    public ResponseEntity<?> toggle(
            @LoginUser SessionUser sessionUser,
            @PathVariable("targetMemberId") Long targetMemberId
    ) {
        boolean isBlocking = memberBlockService.toggleBlock(sessionUser, targetMemberId);
        return ResponseEntity.ok(ApiUtil.success(isBlocking ? "차단했습니다." : "차단을 해제했습니다."));
    }

    @GetMapping
    public ResponseEntity<?> myBlockedList(
            @LoginUser SessionUser sessionUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Slice<?> response = memberBlockService.blockedSlice(sessionUser, pageable);
        return ResponseEntity.ok(ApiUtil.success(response));
    }
}
