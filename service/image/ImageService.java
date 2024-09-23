package com.vulcan.smartcart.service.image;

import com.vulcan.smartcart.dto.ImageDTO;
import com.vulcan.smartcart.exceptions.ResourceNotFoundException;
import com.vulcan.smartcart.model.Image;
import com.vulcan.smartcart.model.Product;
import com.vulcan.smartcart.repository.ImageRepository;
import com.vulcan.smartcart.service.order.OrderService;
import com.vulcan.smartcart.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService implements IImageService{
    private final ImageRepository imageRepository;
    private final IProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    @Override
    public Image getImageById(Long id) {
        logger.info("Fetching image with ID: {}", id);
        return imageRepository.findById(id).orElseThrow(() -> {
            logger.error("Image with ID: {} not found", id);
            return new ResourceNotFoundException("No image found with id: " + id);
        });
    }

    @Override
    public void deleteImageById(Long id) {
        logger.info("Deleting image with ID: {}", id);
        imageRepository.findById(id).ifPresentOrElse(image -> {
            imageRepository.delete(image);
            logger.info("Image with ID: {} deleted successfully", id);
        }, () -> {
            logger.error("Image with ID: {} not found for deletion", id);
            throw new ResourceNotFoundException("No image found with id: " + id);
        });
    }

    @Override
    public List<ImageDTO> saveImages(List<MultipartFile> files, Long productId) {
        logger.info("Saving images for product with ID: {}", productId);
        Product product = productService.getProductById(productId);
        List<ImageDTO> savedImageDto = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                logger.info("Saving image: {}", file.getOriginalFilename());
                Image image = new Image();
                image.setFileName(file.getOriginalFilename());
                image.setFileType(file.getContentType());
                image.setImage(new SerialBlob(file.getBytes()));
                image.setProduct(product);

                String buildDownloadUrl = "/api/v1/images/image/download/";
                Image savedImage = imageRepository.save(image);

                savedImage.setDownloadUrl(buildDownloadUrl + savedImage.getId());
                imageRepository.save(savedImage);

                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setId(savedImage.getId());
                imageDTO.setFileName(savedImage.getFileName());
                imageDTO.setDownloadUrl(savedImage.getDownloadUrl());
                savedImageDto.add(imageDTO);

                logger.info("Image {} saved with ID: {}", file.getOriginalFilename(), savedImage.getId());
            } catch (IOException | SQLException e) {
                logger.error("Error while saving image: {}", file.getOriginalFilename(), e);
                throw new RuntimeException(e.getMessage());
            }
        }

        return savedImageDto;
    }

    @Override
    public void updateImage(MultipartFile file, Long imageId) {
        logger.info("Updating image with ID: {}", imageId);
        Image image = getImageById(imageId);
        try {
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setImage(new SerialBlob(file.getBytes()));
            imageRepository.save(image);
            logger.info("Image with ID: {} updated successfully", imageId);
        } catch (IOException | SQLException e) {
            logger.error("Error while updating image with ID: {}", imageId, e);
            throw new RuntimeException(e.getMessage());
        }
    }

}
