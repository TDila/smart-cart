package com.vulcan.smartcart.repository;

import com.vulcan.smartcart.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
