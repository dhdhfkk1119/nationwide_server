package com.nationwide.nationwide_server.terms;

import com.nationwide.nationwide_server.core.errors.exception.Exception404;
import com.nationwide.nationwide_server.terms.dto.TermsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {
    private final TermsRepository termsRepository;
    
    public Terms findByTermsId(Long id){
        return termsRepository.findById(id).orElseThrow(() -> new Exception404("해당 약관을 찾을 수 없습니다"));
    }

    public TermsResponseDTO.TermsItem findById(Long id){
        return new TermsResponseDTO.TermsItem(findByTermsId(id));
    }

    public TermsResponseDTO.TermsList findAllTerms(){
        List<Terms> terms = termsRepository.findAll();

        List<TermsResponseDTO.TermsItem> termsItems = terms.stream()
                .map(TermsResponseDTO.TermsItem::new)
                .collect(Collectors.toList());

        return new TermsResponseDTO.TermsList(termsItems);
    }
}
