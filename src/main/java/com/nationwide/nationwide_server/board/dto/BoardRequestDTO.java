package com.nationwide.nationwide_server.board.dto;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.image_file.ImageFile;
import com.nationwide.nationwide_server.member.Member;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class BoardRequestDTO {

    @Data
    public static class SaveDTO{
        private String title;
        private String content;
        private Member member;

        public Board toEntity(Member member){
            return Board.builder()
                    .title(this.title)
                    .content(this.content)
                    .member(member)
                    .build();
        }
    }


    @Data
    public static class UpdateDTO{
        private String title;
        private String content;
        private List<String> imageFileIds;
    }


}
