package com.nationwide.nationwide_server.message.dto;

import lombok.Data;

public class MessageRequestDTO {

    @Data
    public static class CreateThreadDTO {
        private Long targetMemberId;
    }

    @Data
    public static class SendMessageDTO {
        private Long threadId;
        private String content;
    }
}
