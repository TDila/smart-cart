package com.vulcan.smartcart.controller;

import com.vulcan.smartcart.dto.ProductDto;
import com.vulcan.smartcart.exceptions.AlreadyExistsException;
import com.vulcan.smartcart.model.Product;
import com.vulcan.smartcart.request.AddProductRequest;
import com.vulcan.smartcart.request.UpdateProductRequest;
import com.vulcan.smartcart.response.ApiResponse;
import com.vulcan.smartcart.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {
    private final IProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts() {
        logger.info("Fetching all products");
        List<Product> products = productService.getAllProducts();
        List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
        logger.info("Total products fetched: {}", convertedProducts.size());
        return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long productId) {
        logger.info("Fetching product with ID: {}", productId);
        try {
            Product product = productService.getProductById(productId);
            ProductDto productDTO = productService.convertToDTO(product);
            logger.info("Product fetched successfully: {}", productDTO);
            return ResponseEntity.ok(new ApiResponse("success", productDTO));
        } catch (Exception e) {
            logger.error("Error fetching product with ID {}: {}", productId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductRequest product) {
        logger.info("Adding product: {}", product);
        try {
            Product theProduct = productService.addProduct(product);
            ProductDto productDTO = productService.convertToDTO(theProduct);
            logger.info("Product added successfully: {}", productDTO);
            return ResponseEntity.ok(new ApiResponse("Add product success!", productDTO));
        } catch (AlreadyExistsException e) {
            logger.error("Product already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse> updateProduct(@RequestBody UpdateProductRequest request, @PathVariable Long productId) {
        logger.info("Updating product with ID: {}", productId);
        try {
            Product theProduct = productService.updateProduct(request, productId);
            ProductDto productDTO = productService.convertToDTO(theProduct);
            logger.info("Product updated successfully: {}", productDTO);
            return ResponseEntity.ok(new ApiResponse("Update product success!", productDTO));
        } catch (Exception e) {
            logger.error("Error updating product with ID {}: {}", productId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long productId) {
        logger.info("Deleting product with ID: {}", productId);
        try {
            productService.deleteProductById(productId);
            logger.info("Product deleted successfully: ID {}", productId);
            return ResponseEntity.ok(new ApiResponse("Delete product success!", productId));
        } catch (Exception e) {
            logger.error("Error deleting product with ID {}: {}", productId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/filter/brand-and-name")
    public ResponseEntity<ApiResponse> getProductByBrandAndName(@RequestParam String brand, @RequestParam String name) {
        logger.info("Fetching products by brand: {} and name: {}", brand, name);
        try {
            List<Product> products = productService.getProductsByBrandAndName(brand, name);
            if (products.isEmpty()) {
                logger.warn("No products found for brand: {} and name: {}", brand, name);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            logger.info("Total products found: {}", convertedProducts.size());
            return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
        } catch (Exception e) {
            logger.error("Error fetching products by brand: {} and name: {}: {}", brand, name, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/filter/category-and-brand")
    public ResponseEntity<ApiResponse> getProductByCategoryAndBrand(@RequestParam String category, @RequestParam String brand){
        logger.info("Fetching products by category: {} and brand: {}", category, brand);
        try {
            List<Product> products = productService.getProductsByCategoryAndBrand(category, brand);
            if(products.isEmpty()){
                logger.warn("No products found for category: {} and brand: {}", category, brand);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            logger.info("Total products found: {}", convertedProducts.size());
            return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
        } catch (Exception e) {
            logger.error("Error fetching products by category: {} and brand: {}: {}", category, brand, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }
    @GetMapping("/filter/{name}")
    public ResponseEntity<ApiResponse> getProductByName(@PathVariable String name){
        logger.info("Fetching products by product name: {}", name);
        try {
            List<Product> products = productService.getProductsByName(name);
            if(products.isEmpty()){
                logger.warn("No products found for name: {}", name);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            logger.info("Total products found: {}", convertedProducts.size());
            return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
        } catch (Exception e) {
            logger.error("Error fetching products by product name: {}: {}", name, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @GetMapping("/filter/brand")
    public ResponseEntity<ApiResponse> getProductByBrand(@RequestParam String brand){
        logger.info("Fetching products by brand: {}", brand);
        try {
            List<Product> products = productService.getProductsByBrand(brand);
            if(products.isEmpty()){
                logger.warn("No products found for brand: {}", brand);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            logger.info("Total products found: {}", convertedProducts.size());
            return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
        } catch (Exception e) {
            logger.error("Error fetching products by brand: {}: {}", brand, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }
    @GetMapping("/filter/category")
    public ResponseEntity<ApiResponse> findProductsByCategory(@RequestParam String category){
        logger.info("Fetching products by category: {}", category);
        try {
            List<Product> products = productService.getProductsByCategory(category);
            if(products.isEmpty()){
                logger.warn("No products found for category: {}", category);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            logger.info("Total products found: {}", convertedProducts.size());
            return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
        } catch (Exception e) {
            logger.error("Error fetching products by category: {}: {}", category, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }
    @GetMapping("/count/brand-and-name")
    public ResponseEntity<ApiResponse> countProductsByBrandAndName(@RequestParam String brand, @RequestParam String name){
        logger.info("Counting products by brand: {} and product name: {}", brand, name);
        try {
            long productCount = productService.countProductsByBrandAndName(brand, name);
            logger.info("Total products found: {}", productCount);
            return ResponseEntity.ok(new ApiResponse("Product count!", productCount));
        } catch (Exception e) {
            logger.error("Error counting products by brand: {} and product name: {}: {}", brand, name, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }
}
