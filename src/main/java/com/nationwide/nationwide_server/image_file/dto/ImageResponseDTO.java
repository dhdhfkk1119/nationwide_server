package com.nationwide.nationwide_server.image_file.dto;

import com.nationwide.nationwide_server._core._enum.FileDBType;
import com.nationwide.nationwide_server.image_file.ImageFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageResponseDTO {
    private String imageFileId;
    private String imageFilePath;
    private String imageFileName;
    private FileDBType fileDbType;
    private String createdAt;
    private String updateAt;

    public ImageResponseDTO (ImageFile imageFile){
        this.imageFileId = imageFile.getImageFileId();
        this.imageFilePath = imageFile.getImageFilePath();
        this.imageFileName = imageFile.getImageFileName();
        this.fileDbType = imageFile.getFileDbType();
        this.createdAt = imageFile.getCreatedTime();
        this.updateAt = imageFile.getUpdatedTime();

    }

}
