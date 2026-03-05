package com.nationwide.nationwide_server.image_file.dto;

import com.nationwide.nationwide_server._core._enum.FileDBType;
import com.nationwide.nationwide_server.image_file.ImageFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageRequestDTO {

    private String imageFileId;
    private String imageFilePath; // 이미지 경로
    private String imageFileName; // 이미지 이름
    private FileDBType fileDBType;
    private LocalDateTime createdAt;

    public ImageFile toEntity(){
        return ImageFile.builder()
                .imageFileId(this.imageFileId)
                .imageFilePath(this.imageFilePath)
                .imageFileName(this.imageFileName)
                .fileDbType(this.fileDBType)
                .createdAt(this.createdAt)
                .build();
    }
}
