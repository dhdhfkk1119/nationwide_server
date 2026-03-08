package com.nationwide.nationwide_server.image_file;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server._core._enum.FileDBType;
import com.nationwide.nationwide_server._core.util.TimeFormatUtil;
import com.nationwide.nationwide_server.member.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "image_file_tb")
@Entity
@Builder
@ToString(exclude = {"board", "member"})
public class ImageFile {

    @Id
    private String imageFileId; // 이미지 파일 번호 모든 DB imageFileId 랑 매칭

    private String imageFilePath; // 이미지 경로
    private String imageFileName; // 이미지 이름

    @Enumerated(EnumType.STRING)
    private FileDBType fileDbType; // BOARD, MEMBER

    // Board의 이미지일 경우
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;

    public String getCreatedTime(){
        return TimeFormatUtil.localDateTimeFormat(createdAt);
    }

    public String getUpdatedTime(){
        return TimeFormatUtil.localDateTimeFormat(updateAt);
    }
    
    private LocalDateTime delDate; // 삭제 일

    // imageFileId를 UUID로 자동 생성
    @PrePersist
    public void generateImageFileId() {
        if (this.imageFileId == null) {
            this.imageFileId = UUID.randomUUID().toString();
        }
    }
}
