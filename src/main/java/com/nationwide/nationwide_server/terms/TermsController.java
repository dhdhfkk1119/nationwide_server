package com.nationwide.nationwide_server.terms;

import com.nationwide.nationwide_server.core.util.ApiUtil;
import com.nationwide.nationwide_server.terms.dto.TermsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsController {
    private final TermsService termsService;

    @GetMapping("/list")
    public ResponseEntity<?> findByAll(){
        TermsResponseDTO.TermsList terms = termsService.findAllTerms();

        // DTO 객체를 성공적으로 담아 200 OK 응답 반환
        return ResponseEntity.ok(ApiUtil.success(terms));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable(name = "id") Long id){
        TermsResponseDTO.TermsItem term = termsService.findById(id);

        // DTO 객체를 성공적으로 담아 200 OK 응답 반환
        return ResponseEntity.ok(ApiUtil.success(term));
    }
}
