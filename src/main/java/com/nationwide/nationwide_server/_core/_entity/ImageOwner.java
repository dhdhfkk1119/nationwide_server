package com.nationwide.nationwide_server._core._entity;

import com.nationwide.nationwide_server.image_file.ImageFile;

import java.util.List;

public interface ImageOwner {
    List<ImageFile> getImageFiles();
    void addImageFile(ImageFile imageFile);
}
