package com.nationwide.nationwide_server.member;

import com.nationwide.nationwide_server.core.errors.exception.Exception401;
import com.nationwide.nationwide_server.core.util.ApiUtil;
import com.nationwide.nationwide_server.email.EmailService;
import com.nationwide.nationwide_server.member.dto.MemberRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    private final MemberService memberService;
    private final EmailService emailService;

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

    // 중복체크
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable("email")String email){
        log.info("아이디 체크 인증 : {}" , memberService.checkEmail(email));
        return ResponseEntity.ok(memberService.checkEmail(email));
    }


}
