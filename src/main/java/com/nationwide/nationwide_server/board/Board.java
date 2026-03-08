package com.nationwide.nationwide_server.board;

import com.nationwide.nationwide_server.board.dto.BoardRequestDTO;
import com.nationwide.nationwide_server._core._entity.ImageOwner;
import com.nationwide.nationwide_server._core.util.TimeFormatUtil;
import com.nationwide.nationwide_server.image_file.ImageFile;
import com.nationwide.nationwide_server.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"imageFiles", "member"})
@Table(name = "board_tb")
public class Board implements ImageOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    private Long viewCnt;

    // 이미지 파일 이름 저장
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageFile> imageFiles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx")
    private Member member;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    private Timestamp delDate;

    @Override
    public List<ImageFile> getImageFiles() {
        return imageFiles;
    }

    public String getCreatedTime(){
        return TimeFormatUtil.timestampFormat(createdAt);
    }

    public String getUpdatedTime(){
        return TimeFormatUtil.timestampFormat(updatedAt);
    }

    public void updateBoard(BoardRequestDTO.UpdateDTO dto) {
        if (dto.getTitle() != null) {
            this.title = dto.getTitle();
        }
        if (dto.getContent() != null) {
            this.content = dto.getContent();
        }
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public void addImageFile(ImageFile imageFile) {
        this.imageFiles.add(imageFile);
        imageFile.setBoard(this);
    }

    public boolean getIsMine(Long memberIdx){
        return this.getMember().getIsMine(memberIdx);
    }

}
