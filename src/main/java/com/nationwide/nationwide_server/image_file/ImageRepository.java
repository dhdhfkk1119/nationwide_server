package com.nationwide.nationwide_server.image_file;

import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageFile,String> {
    List<ImageFile> findByBoardId(Long boardId);

    @Query("SELECT i FROM ImageFile i WHERE i.imageFileId = :imageFileId")
    List<ImageFile> findByImageFileId(String imageFileId);

    @Query("SELECT i FROM ImageFile i WHERE i.imageFileId = :imageFileId")
    ImageFile findByOneImageFileId(String imageFileId);



}
