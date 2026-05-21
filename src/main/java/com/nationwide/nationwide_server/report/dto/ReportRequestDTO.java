package com.nationwide.nationwide_server.report.dto;

import lombok.Data;

public class ReportRequestDTO {

    @Data
    public static class CreateDTO {
        private String reporterComment;
    }

    @Data
    public static class ProcessDTO {
        private String adminComment;
    }
}
