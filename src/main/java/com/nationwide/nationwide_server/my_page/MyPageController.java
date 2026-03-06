package com.nationwide.nationwide_server.my_page;

import com.nationwide.nationwide_server.board.dto.BoardResponseDTO;
import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/my-page")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    @GetMapping("/favorite-boards")
    public ResponseEntity<?> favoriteBoardSlice(@LoginUser SessionUser sessionUser, Pageable pageable) {
        Slice<BoardResponseDTO.ListDTO> response = myPageService.favoriteBoardSlice(sessionUser, pageable);
        return ResponseEntity.ok(ApiUtil.success(response));
    }

}
