package com.vulcan.smartcart.controller;

import com.vulcan.smartcart.dto.ImageDTO;
import com.vulcan.smartcart.exceptions.ResourceNotFoundException;
import com.vulcan.smartcart.model.Image;
import com.vulcan.smartcart.response.ApiResponse;
import com.vulcan.smartcart.service.image.IImageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/images")
public class ImageController {
    private final IImageService imageService;

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> saveImages(
            @RequestParam("files") List<MultipartFile> files
            ,@RequestParam("productId") Long productId){
        try {
            List<ImageDTO> imageDTOS = imageService.saveImages(files, productId);
            log.info("Images uploaded successfully for product ID: {}", productId);
            return ResponseEntity.ok(new ApiResponse("Upload success", imageDTOS));
        } catch (Exception e) {
            log.error("Image upload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Upload failed!", e.getMessage()));
        }
    }

    @GetMapping("/image/download/{imageId}")
    public ResponseEntity<Resource> downloadImage(@PathVariable Long imageId) throws SQLException {
        try {
            Image image = imageService.getImageById(imageId);
            ByteArrayResource resource = new ByteArrayResource(image.getImage().getBytes(1, (int) image.getImage().length()));
            log.info("Image downloaded successfully: ID = {}", imageId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(image.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                    .body(resource);
        } catch (ResourceNotFoundException e) {
            log.warn("Image not found for download: ID = {}", imageId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{imageId}")
    public ResponseEntity<ApiResponse> updateImage(@PathVariable Long imageId, @RequestBody MultipartFile file){
        try {
            imageService.updateImage(file, imageId);
            log.info("Image updated successfully: ID = {}", imageId);
            return ResponseEntity.ok(new ApiResponse("Update success", null));
        } catch (ResourceNotFoundException e) {
            log.warn("Image not found for update: ID = {}", imageId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Image update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Update failed!", e.getMessage()));
        }
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable Long imageId){
        try {
            imageService.deleteImageById(imageId);
            log.info("Image deleted successfully: ID = {}", imageId);
            return ResponseEntity.ok(new ApiResponse("Delete success", null));
        } catch (ResourceNotFoundException e) {
            log.warn("Image not found for deletion: ID = {}", imageId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Image deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Delete failed!", e.getMessage()));
        }
    }
}
