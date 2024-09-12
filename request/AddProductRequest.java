package com.vulcan.smartcart.request;

import com.vulcan.smartcart.model.Category;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddProductRequest {
    private Long id;
    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private Integer inventory;
    private Category category;
}
