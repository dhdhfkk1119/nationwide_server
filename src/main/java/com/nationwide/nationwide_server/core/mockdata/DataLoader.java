package com.nationwide.nationwide_server.core.mockdata;

import com.nationwide.nationwide_server.terms.Terms;
import com.nationwide.nationwide_server.terms.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final TermsRepository termsRepository;

    @Override
    public void run(String... args) throws Exception {
        if (termsRepository.count() == 0) { // DB가 비어있을 때만 추가
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

            termsRepository.saveAll(Arrays.asList(terms1, terms2, terms3));

            System.out.println("더미 약관 데이터 초기화 완료!");
        }
    }
}
