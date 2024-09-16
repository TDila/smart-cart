package com.vulcan.smartcart.dto;

import com.vulcan.smartcart.model.Category;
import com.vulcan.smartcart.model.Image;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private Integer inventory;
    private Category category;
    private List<ImageDTO> images;
}
