package com.nationwide.nationwide_server.member;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core._enum.ResourceType;
import com.nationwide.nationwide_server._core.errors.exception.Exception401;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.member.dto.MemberRequestDTO;
import com.nationwide.nationwide_server.member.dto.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/save")
    public ResponseEntity<?> saveMember(@RequestBody MemberRequestDTO.SaveDTO dto) {
        if (!dto.getPassword().equals(dto.getRePassword())) {
            throw new Exception401("비밀번호가 일치하지 않습니다");
        }

        if (dto.getBirthDate().isAfter(LocalDate.now())) {
            throw new Exception401("생년월일이 미래입니다");
        }

        memberService.save(dto);
        return ResponseEntity.ok(ApiUtil.success(ResourceType.MEMBER.getSaveSuccess()));
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(memberService.existsByLoginId(email));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginMember(@RequestBody MemberRequestDTO.LoginDTO dto) {
        MemberResponseDTO.LoginDTO loginDTO = memberService.loginMember(dto);
        return ResponseEntity.ok(ApiUtil.success(loginDTO));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@LoginUser SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다");
        }

        MemberResponseDTO.DetailDTO dto = memberService.detail(sessionUser.getId());
        return ResponseEntity.ok(ApiUtil.success(dto));
    }

    @PutMapping("/update/{memberIdx}")
    public ResponseEntity<?> updateMember(
            @LoginUser SessionUser sessionUser,
            @PathVariable("memberIdx") Long memberIdx,
            @RequestPart("dto") MemberRequestDTO.UpdateDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> file
    ) {
        memberService.updateMember(memberIdx, sessionUser, dto, file);
        return ResponseEntity.ok(ApiUtil.success(ResourceType.MEMBER.getUpdateSuccess()));
    }
}
