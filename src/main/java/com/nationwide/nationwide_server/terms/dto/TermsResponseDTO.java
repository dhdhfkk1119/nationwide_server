package com.nationwide.nationwide_server.terms.dto;

import com.nationwide.nationwide_server.terms.Terms;
import lombok.Data;

public class TermsResponseDTO {

    @Data
    public static class TermsList{
        private Long id;
        private String title;
        private String content;
        private boolean isRequire;

        public TermsList(Terms terms){
            this.id = terms.getId();
            this.title = terms.getTitle();
            this.content = terms.getContent();
            this.isRequire = terms.isRequired();
        }
    }
}
