package com.nationwide.nationwide_server.member;

import com.nationwide.nationwide_server.core.errors.exception.Exception401;
import com.nationwide.nationwide_server.core.util.ApiUtil;
import com.nationwide.nationwide_server.member.dto.MemberRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/save")
    public ResponseEntity<?> saveMember(@RequestBody MemberRequestDTO.SaveDTO dto){

        // 비밀번호 암호화
        if(!dto.getPassword().equals(dto.getRePassword())){
            throw new Exception401("비밀번호가 일치하지 않습니다");
        }


        if(dto.getBirthDate().isAfter(LocalDate.now())){
            throw new Exception401("생년월일이 미래입니다");
        }


        memberService.save(dto);
        return ResponseEntity.ok(ApiUtil.success("회원가입 성공"));
    }

}
