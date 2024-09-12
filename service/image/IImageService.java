package com.vulcan.smartcart.service.image;

import com.vulcan.smartcart.dto.ImageDTO;
import com.vulcan.smartcart.model.Image;
import com.vulcan.smartcart.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {
    Image getImageById(Long id);
    void deleteImageById(Long id);
    List<ImageDTO> saveImages(List<MultipartFile> files, Long productId);
    void updateImage(MultipartFile file, Long imageId);
}
