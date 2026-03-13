package com.nationwide.nationwide_server.my_page;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.board.dto.BoardResponseDTO;
import com.nationwide.nationwide_server.my_page.dto.MyPageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/my-page")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    @GetMapping("/summary")
    public ResponseEntity<?> summary(@LoginUser SessionUser sessionUser) {
        MyPageResponseDTO.SummaryDTO response = myPageService.summary(sessionUser);
        return ResponseEntity.ok(ApiUtil.success(response));
    }

    @GetMapping("/boards")
    public ResponseEntity<?> boardSlice(
            @LoginUser SessionUser sessionUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Slice<BoardResponseDTO.ListDTO> response = myPageService.boardSlice(sessionUser, pageable);
        return ResponseEntity.ok(ApiUtil.success(response));
    }

    @GetMapping("/comments")
    public ResponseEntity<?> commentSlice(
            @LoginUser SessionUser sessionUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Slice<MyPageResponseDTO.CommentListDTO> response = myPageService.commentSlice(sessionUser, pageable);
        return ResponseEntity.ok(ApiUtil.success(response));
    }

    @GetMapping("/favorite-boards")
    public ResponseEntity<?> favoriteBoardSlice(
            @LoginUser SessionUser sessionUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Slice<BoardResponseDTO.ListDTO> response = myPageService.favoriteBoardSlice(sessionUser, pageable);
        return ResponseEntity.ok(ApiUtil.success(response));
    }

    @GetMapping("/members/{memberId}/summary")
    public ResponseEntity<?> memberSummary(
            @LoginUser SessionUser sessionUser,
            @PathVariable("memberId") Long memberId
    ) {
        MyPageResponseDTO.SummaryDTO response = myPageService.memberSummary(sessionUser, memberId);
        return ResponseEntity.ok(ApiUtil.success(response));
    }

    @GetMapping("/members/{memberId}/boards")
    public ResponseEntity<?> memberBoardSlice(
            @LoginUser SessionUser sessionUser,
            @PathVariable("memberId") Long memberId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Slice<BoardResponseDTO.ListDTO> response = myPageService.memberBoardSlice(sessionUser, memberId, pageable);
        return ResponseEntity.ok(ApiUtil.success(response));
    }
}
