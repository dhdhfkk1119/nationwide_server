package com.nationwide.nationwide_server.terms.dto;

import com.nationwide.nationwide_server.terms.Terms;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

public class TermsResponseDTO {

    @Data
    public static class TermsItem {
        private Long id;
        private String title;
        private Boolean required; // 필수 동의 여부

        public TermsItem(Terms terms) {
            this.id = terms.getId();
            this.title = terms.getTitle();
            this.required = terms.isRequired(); // Terms 엔티티에 해당 필드가 있다고 가정
        }

    }

    @Data
    public static class TermsList {
        private List<TermsItem> terms;

        public TermsList(List<TermsItem> termsItems) {
            this.terms = termsItems;
        }


    }
}