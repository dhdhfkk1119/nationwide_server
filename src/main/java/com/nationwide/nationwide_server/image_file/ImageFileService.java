package com.nationwide.nationwide_server.image_file;

import com.nationwide.nationwide_server._core._entity.ImageOwner;
import com.nationwide.nationwide_server._core._enum.FileDBType;
import com.nationwide.nationwide_server._core.config.UploadProperties;
import com.nationwide.nationwide_server._core.util.UploadFileUtil;
import com.nationwide.nationwide_server.image_file.dto.ImageRequestDTO;
import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageFileService {
    private final ImageRepository imageRepository;
    private final UploadFileUtil uploadFileUtil;
    private final UploadProperties uploadProperties;

    // 다중 이미지 경로 찾기
    public List<ImageResponseDTO> imageFileDetailListInfo(String imageFileId){
        List<ImageFile> imageFiles = imageRepository.findByImageFileId(imageFileId);
        return imageFiles.stream()
                .map(ImageResponseDTO::new)
                .toList();
    }
    // 단일 이미지 경로 찾기
    public ImageResponseDTO imageFileDetailOneInfo(String imageFileId){
        ImageFile imageFile = imageRepository.findByOneImageFileId(imageFileId);
        return new ImageResponseDTO(imageFile);
    }

    // 이미지 엔티티 생성 및 저장
    public List<ImageFile> saveImage(List<MultipartFile> files,FileDBType dbType){

        try {
            List<UploadFileUtil.UploadFileUtilDTO> uploadedFilePaths =
                    uploadFileUtil.uploadImages(
                            files.toArray(new MultipartFile[0]),
                            uploadProperties.getImageDir()
                    );

            // 각 업로드된 파일 경로로 ImageFile 엔티티 생성
            List<ImageFile> imageFiles = new ArrayList<>();

            for(UploadFileUtil.UploadFileUtilDTO filePath : uploadedFilePaths) {

                // ImageRequestDTO Builder 사용
                ImageFile imageFile = ImageRequestDTO.builder()
                        .imageFileId(filePath.getImageFileId())
                        .imageFilePath(filePath.getFilePath())
                        .imageFileName(filePath.getFileName())
                        .fileDBType(dbType)
                        .build()
                        .toEntity();
                imageFiles.add(imageFile);

            }

            return imageFiles;
        } catch (IOException e){
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다", e);
        }

    }

    // ImageFileId 로 이미지 비교 이후 파일 삭제 및 업데이트
    public void syncDeleteImages(ImageOwner owner, List<String> newImageFileIds) {
        Set<String> newImageIdSet = new HashSet<>(newImageFileIds);

        Map<String, ImageFile> existingImages = owner.getImageFiles().stream()
                .collect(Collectors.toMap(
                        ImageFile::getImageFileId,
                        imageFile -> imageFile
                ));

        // 삭제 (orphanRemoval이 자동으로 DB 삭제 처리)
        List<ImageFile> imagesToDelete = owner.getImageFiles().stream()
                .filter(imageFile -> !newImageIdSet.contains(imageFile.getImageFileId()))
                .toList();

        for (ImageFile imageFile : imagesToDelete) {
            owner.getImageFiles().remove(imageFile); // 양방햐 관계 제거
            imageRepository.delete(imageFile); // DB 삭제
            uploadFileUtil.deleteImage(imageFile); // 파일 삭제 (마지막에)
        }

    }

    // 새 이미지 추가 다른 이미지 업데이트 용
    public void addNewImages(ImageOwner owner, List<MultipartFile> newImages) {
        try {
            // 파일 업로드
            List<UploadFileUtil.UploadFileUtilDTO> uploadedFiles =
                    uploadFileUtil.uploadImages(
                            newImages.toArray(new MultipartFile[0]),
                            uploadProperties.getImageDir()
                    );

            // ImageFile 엔티티 생성 및 DB 연결
            for (UploadFileUtil.UploadFileUtilDTO dto : uploadedFiles) {
                ImageFile imageFile = ImageFile.builder()
                        .imageFileId(dto.getImageFileId())
                        .imageFilePath(dto.getFilePath())
                        .imageFileName(dto.getFileName())
                        .fileDbType(FileDBType.BOARD)
                        .build();

                // 해당 DB 에 추가 (cascade로 자동 저장)
                owner.addImageFile(imageFile);
            }

        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다", e);
        }
    }
}
