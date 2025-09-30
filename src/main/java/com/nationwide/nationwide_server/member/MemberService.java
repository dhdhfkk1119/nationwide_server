package com.nationwide.nationwide_server.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    // 회원 가입
    @Transactional
    public void save(MemberRequestDTO.SaveDTO saveDTO){
        Member member = saveDTO.toEntity();
        // 비밀번호 암호화
        if(saveDTO.getPassword().equals(saveDTO.getRePassword())){

        }
        member.setPassword(bCryptPasswordEncoder.encode(member.getPassword()));
        memberRepository.save(member);
    }

    // 회원 유저 정보 찾기
    public MemberResponseDTO.DetailDTO detail(Long memberId){
        Member member = findById(memberId);
        return new MemberResponseDTO.DetailDTO(member);
    }



    // 회원 Member 반환 로직
    public Member findById(Long memberId){
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다"));
    }
}
