package com.nationwide.nationwide_server.member;

import com.nationwide.nationwide_server.core.errors.exception.Exception400;
import com.nationwide.nationwide_server.core.errors.exception.Exception401;
import com.nationwide.nationwide_server.core.errors.exception.Exception404;
import com.nationwide.nationwide_server.core.jwt.JwtTokenProvider;
import com.nationwide.nationwide_server.email.EmailRepository;
import com.nationwide.nationwide_server.email.EmailService;
import com.nationwide.nationwide_server.member.dto.MemberRequestDTO;
import com.nationwide.nationwide_server.member.dto.MemberResponseDTO;
import com.nationwide.nationwide_server.member_terms.MemberTerms;
import com.nationwide.nationwide_server.member_terms.MemberTermsRepository;
import com.nationwide.nationwide_server.terms.Terms;
import com.nationwide.nationwide_server.terms.TermsRepository;
import com.nationwide.nationwide_server.terms.TermsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberTermsRepository memberTermsRepository;
    private final TermsService termsService;
    private final EmailRepository emailRepository;
    private final EmailService emailService;
    private final TermsRepository termsRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원 가입
    @Transactional
    public void save(MemberRequestDTO.SaveDTO saveDTO){
        Member member = saveDTO.toEntity();

        emailService.findByLoginId(saveDTO.getLoginId());

        if(!emailRepository.existsByLoginId(saveDTO.getLoginId())){
            throw new Exception400("이메일 인증이 완료 되지 않았습니다");
        }

        // 아이디 중복 검사
        if(existsByLoginId(saveDTO.getLoginId())){
            throw new Exception401("이미 가입 된 유저입니다");
        }


        // 1. 모든 필수 약관 가져오기
        List<Terms> requiredTerms = termsRepository.findByIsRequired();

        // 2. 필수 약관 ID 목록
        List<Long> requiredIds = requiredTerms.stream()
                .map(Terms::getId)
                .collect(Collectors.toList());


        if (!saveDTO.getAgreedTermsIds().containsAll(requiredIds)) {
            throw new Exception400("필수 약관에 동의해야 합니다");
        }


        member.setPassword(bCryptPasswordEncoder.encode(member.getPassword()));
        memberRepository.save(member);

        List<MemberTerms> memberTermsList = new ArrayList<>();

        // 약관 동의
        for(Long terms : saveDTO.getAgreedTermsIds()){

            // 약관 유무 체크
            Terms termsTo = termsService.findByTermsId(terms);
            memberTermsList.add( MemberTerms.builder()
                    .memberId(member)
                    .termsId(termsTo)
                    .build());

        }
        memberTermsRepository.saveAll(memberTermsList);

        emailRepository.deleteByLoginId(saveDTO.getLoginId());
    }

    // 회원 로그인
    public MemberResponseDTO.LoginDTO loginMember(MemberRequestDTO.LoginDTO dto){
        Member member = findByLoginId(dto.getLoginId());

        if(!bCryptPasswordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new Exception401("비밀번호가 일치하지 않습니다");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member);

        String refreshToken = null;
        if(dto.isAutoLogin()){
            refreshToken = jwtTokenProvider.createRefreshToken(member);
        }

        Long expiresIn = jwtTokenProvider.getAccessExpirationSeconds();


        return new MemberResponseDTO.LoginDTO(member,accessToken,refreshToken,expiresIn);
    }


    // 회원 유저 정보 찾기
    public MemberResponseDTO.DetailDTO detail(Long memberId){
        Member member = findById(memberId);
        return new MemberResponseDTO.DetailDTO(member);
    }


    // 회원 고유 번호 Member 유무 검사
    public Member findById(Long memberId){
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new Exception404("회원을 찾을 수 없습니다"));
    }

    // 회원 로그인 아이디 Member 이메일 유무 검사
    public boolean existsByLoginId(String loginId){
        return memberRepository.existsByLoginId(loginId);
    }

    public Member findByLoginId(String loginId){
        return memberRepository.findByLoginId(loginId).orElseThrow(() -> new Exception404("아이디가 존재 하지 않습니다"));
    }



}
