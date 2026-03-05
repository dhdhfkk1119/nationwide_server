package com.nationwide.nationwide_server._core.util;

import com.nationwide.nationwide_server._core.config.UploadProperties;
import com.nationwide.nationwide_server.image_file.ImageFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Data
@Slf4j
public class UploadFileUtil {
    private final UploadProperties uploadProperties;

    @Data
    @AllArgsConstructor
    public static class UploadFileUtilDTO {
        private String filePath;      // 전체 경로
        private String fileName;      // 파일명
        private String imageFileId;          // UUID 부분
    }

    // 다중 이미지 업로드
    public List<UploadFileUtilDTO> uploadImages(MultipartFile[] multipartFiles, String dir) throws IOException {
        String fullUploadPath = Paths.get(uploadProperties.getRootDir(),dir).toString();

        createUploadDirectory(fullUploadPath);

        List<String> fileNames = new ArrayList<>();
        List<UploadFileUtilDTO> fileUtilDTOS = new ArrayList<>();

        for(MultipartFile file : multipartFiles) {
            String originFilename = file.getOriginalFilename();
            String extension = getFileExtension(originFilename);
            String uniqueFileName = generateUniqueFileName(extension);
            Path filePath = Paths.get(fullUploadPath, uniqueFileName);
            file.transferTo(filePath);

            // DTO에 따로 저장
            fileUtilDTOS.add(new UploadFileUtilDTO(filePath.toString(),originFilename,uniqueFileName));

            fileNames.add(filePath.toString());
        }

        return fileUtilDTOS;
    }

    // 단일 이미지 업로드
    public UploadFileUtilDTO uploadImage(MultipartFile multipartFiles, String dir) throws IOException{
        String fullUploadPath = Paths.get(uploadProperties.getRootDir(),dir).toString();

        createUploadDirectory(fullUploadPath);
        String originFilename = multipartFiles.getOriginalFilename();
        String extension = getFileExtension(originFilename);
        String uniqueFileName = generateUniqueFileName(extension);
        Path filePath = Paths.get(fullUploadPath, uniqueFileName);
        multipartFiles.transferTo(filePath);

        return new UploadFileUtilDTO(filePath.toString(),originFilename,uniqueFileName);
    }

    private String generateUniqueFileName(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMDD_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0,8);
        return timestamp +  "_" + uuid + extension;
    }

    // 파일 확장자만 추출 해주는 메서드
    private String getFileExtension(String originFilename) {
        if(originFilename == null || originFilename.lastIndexOf(".") == -1){
            return "";
        }
        return originFilename.substring(originFilename.lastIndexOf("."));
    }

    // 폴더를 생성하는 메서드
    private void createUploadDirectory(String fullUploadPath) throws IOException{
        Path uploadPath = Paths.get(fullUploadPath);

        if(!Files.exists(uploadPath)){
            Files.createDirectories(uploadPath);
        }
    }

    // 단일 이미지 삭제
    public void deleteImage(ImageFile imageFile) {
        if (imageFile == null || imageFile.getImageFilePath() == null) {
            return;
        }

        try {
            Path filePath = Paths.get(imageFile.getImageFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 로그 남기고 예외는 던지지 않음 (파일이 이미 없을 수도 있음)
            log.warn("이미지 파일 삭제 실패: {}", imageFile.getImageFilePath(), e);
        }
    }

    // 여러 이미지 삭제
    public void deleteImages(List<ImageFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return;
        }

        for (ImageFile imageFile : imageFiles) {
            deleteImage(imageFile);
        }
    }

    // 메서드명 변경 (더 명확하게)
    @Deprecated
    public void deleteProfileImage(ImageFile imageFile) {
        deleteImage(imageFile);
    }
}
