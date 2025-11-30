package com.nationwide.nationwide_server.terms;

import com.nationwide.nationwide_server.core.errors.exception.Exception404;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {
    private final TermsRepository termsRepository;
    
    public Terms findByTermsId(Long id){
        return termsRepository.findById(id).orElseThrow(() -> new Exception404("해당 약관을 찾을 수 없습니다"));
    }
}
