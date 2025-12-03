package com.nationwide.nationwide_server.core.mockdata;

import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberRepository;
import com.nationwide.nationwide_server.member.m_enum.Gender;
import com.nationwide.nationwide_server.member.m_enum.LoginType;
import com.nationwide.nationwide_server.member.m_enum.UserRole;
import com.nationwide.nationwide_server.member_terms.MemberTerms;
import com.nationwide.nationwide_server.member_terms.MemberTermsRepository;
import com.nationwide.nationwide_server.terms.Terms;
import com.nationwide.nationwide_server.terms.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final TermsRepository termsRepository;
    private final MemberRepository memberRepository;
    private final MemberTermsRepository memberTermsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        List<Terms> termsList;


        if (termsRepository.count() == 0) {
            Terms terms1 = Terms.builder()
                    .title("서비스 이용 약관")
                    .content("이용 약관 내용 예시입니다.")
                    .isRequired(true)
                    .build();

            Terms terms2 = Terms.builder()
                    .title("개인정보 처리 방침")
                    .content("개인정보 처리 방침 내용 예시입니다.")
                    .isRequired(true)
                    .build();

            Terms terms3 = Terms.builder()
                    .title("마케팅 정보 수신 동의")
                    .content("마케팅 정보 수신 동의 내용 예시입니다.")
                    .isRequired(false)
                    .build();

            termsList = termsRepository.saveAll(Arrays.asList(terms1, terms2, terms3));
            System.out.println("더미 약관 데이터 초기화 완료!");
        } else {
            termsList = termsRepository.findAll();
        }

        List<Member> members;

        if (memberRepository.count() == 0) {

            String encodedPw = passwordEncoder.encode("123456");

            Member admin = Member.builder()
                    .name("관리자")
                    .nickName("AdminMaster")
                    .loginId("admin")
                    .password(encodedPw)
                    .phoneNumber("010-0000-0000")
                    .gender(Gender.MALE)
                    .birth("1990")
                    .date("01-01")
                    .addressNumber("12345")
                    .address("서울시 강남구")
                    .addressDetail("1층")
                    .profileImage("/uploads/member-images/profile_admin.png")
                    .loginType(LoginType.LOCAL)
                    .userRole(UserRole.ADMIN)
                    .isEmailVerified(true)
                    .isPhoneVerified(true)
                    .build();

            Member user1 = Member.builder()
                    .name("정우")
                    .nickName("JJW")
                    .loginId("user1")
                    .password(encodedPw)
                    .phoneNumber("010-1111-1111")
                    .gender(Gender.MALE)
                    .birth("1995")
                    .date("05-20")
                    .addressNumber("54321")
                    .address("부산시 해운대구")
                    .addressDetail("101호")
                    .profileImage("/uploads/member-images/profile1.png")
                    .loginType(LoginType.LOCAL)
                    .userRole(UserRole.USER)
                    .isEmailVerified(true)
                    .isPhoneVerified(false)
                    .build();

            Member user2 = Member.builder()
                    .name("민지")
                    .nickName("MJ")
                    .loginId("user2")
                    .password(encodedPw)
                    .phoneNumber("010-2222-2222")
                    .gender(Gender.FEMALE)
                    .birth("1998")
                    .date("12-10")
                    .addressNumber("67890")
                    .address("서울시 송파구")
                    .addressDetail("201호")
                    .profileImage("/uploads/member-images/profile2.png")
                    .loginType(LoginType.LOCAL)
                    .userRole(UserRole.USER)
                    .isEmailVerified(false)
                    .isPhoneVerified(false)
                    .build();

            members = memberRepository.saveAll(Arrays.asList(admin, user1, user2));

            System.out.println("더미 회원 데이터 3명 초기화 완료!");
        } else {
            members = memberRepository.findAll();
        }

        if (memberTermsRepository.count() == 0) {
            List<MemberTerms> memberTermsList = new ArrayList<>();

            for (Member member : members) {
                for (Terms terms : termsList) {
                    MemberTerms mt = MemberTerms.builder()
                            .memberId(member)
                            .termsId(terms)
                            .build();
                    memberTermsList.add(mt);
                }
            }

            memberTermsRepository.saveAll(memberTermsList);
            System.out.println("회원 약관 동의 데이터 초기화 완료! (총 " + memberTermsList.size() + "개)");
        }
    }
}

