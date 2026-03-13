package com.nationwide.nationwide_server.follow;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.follow.dto.FollowResponseDTO;
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
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @PostMapping("/{targetMemberId}/toggle")
    public ResponseEntity<?> toggle(
            @LoginUser SessionUser sessionUser,
            @PathVariable("targetMemberId") Long targetMemberId
    ) {
        FollowResponseDTO.StatusDTO response = followService.toggleFollow(sessionUser, targetMemberId);
        return ResponseEntity.ok(ApiUtil.success(response));
    }

    @GetMapping("/status/{targetMemberId}")
    public ResponseEntity<?> status(
            @LoginUser SessionUser sessionUser,
            @PathVariable("targetMemberId") Long targetMemberId
    ) {
        Long viewerId = sessionUser != null ? sessionUser.getId() : null;
        FollowResponseDTO.StatusDTO response = followService.getStatus(viewerId, targetMemberId);
        return ResponseEntity.ok(ApiUtil.success(response));
    }

    @GetMapping("/members/{memberId}/followers")
    public ResponseEntity<?> followers(
            @LoginUser SessionUser sessionUser,
            @PathVariable("memberId") Long memberId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Slice<FollowResponseDTO.MemberListDTO> response = followService.followerSlice(sessionUser, memberId, pageable);
        return ResponseEntity.ok(ApiUtil.success(response));
    }

    @GetMapping("/members/{memberId}/following")
    public ResponseEntity<?> following(
            @LoginUser SessionUser sessionUser,
            @PathVariable("memberId") Long memberId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Slice<FollowResponseDTO.MemberListDTO> response = followService.followingSlice(sessionUser, memberId, pageable);
        return ResponseEntity.ok(ApiUtil.success(response));
    }
}
