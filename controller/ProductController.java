package com.vulcan.smartcart.controller;

import com.vulcan.smartcart.dto.ProductDto;
import com.vulcan.smartcart.exceptions.AlreadyExistsException;
import com.vulcan.smartcart.model.Product;
import com.vulcan.smartcart.request.AddProductRequest;
import com.vulcan.smartcart.request.UpdateProductRequest;
import com.vulcan.smartcart.response.ApiResponse;
import com.vulcan.smartcart.service.product.IProductService;
import lombok.RequiredArgsConstructor;
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
    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts(){
        List<Product> products = productService.getAllProducts();
        List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
        return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long productId){
        try {
            Product product = productService.getProductById(productId);
            ProductDto productDTO = productService.convertToDTO(product);
            return ResponseEntity.ok(new ApiResponse("success", productDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductRequest product){
        try {
            Product theProduct = productService.addProduct(product);
            ProductDto productDTO = productService.convertToDTO(theProduct);
            return ResponseEntity.ok(new ApiResponse("Add product success!", productDTO));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse> updateProduct(@RequestBody UpdateProductRequest request, @PathVariable Long productId){
        try {
            Product theProduct = productService.updateProduct(request, productId);
            ProductDto productDTO = productService.convertToDTO(theProduct);
            return ResponseEntity.ok(new ApiResponse("Update product success!", productDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long productId){
        try {
            productService.deleteProductById(productId);
            return ResponseEntity.ok(new ApiResponse("Delete product success!", productId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
    @GetMapping("/filter/brand-and-name")
    public ResponseEntity<ApiResponse> getProductByBrandAndName(@RequestParam String brand, @RequestParam String name){
        try {
            List<Product> products = productService.getProductsByBrandAndName(brand, name);
            if(products.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/filter/category-and-brand")
    public ResponseEntity<ApiResponse> getProductByCategoryAndBrand(@RequestParam String category, @RequestParam String brand){
        try {
            List<Product> products = productService.getProductsByCategoryAndBrand(category, brand);
            if(products.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }
    @GetMapping("/filter/{name}")
    public ResponseEntity<ApiResponse> getProductByName(@PathVariable String name){
        try {
            List<Product> products = productService.getProductsByName(name);
            if(products.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @GetMapping("/filter/brand")
    public ResponseEntity<ApiResponse> getProductByBrand(@RequestParam String brand){
        try {
            List<Product> products = productService.getProductsByBrand(brand);
            if(products.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }
    @GetMapping("/filter/category")
    public ResponseEntity<ApiResponse> findProductsByCategory(@RequestParam String category){
        try {
            List<Product> products = productService.getProductsByCategory(category);
            if(products.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new ApiResponse("success", convertedProducts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }
    @GetMapping("/count/brand-and-name")
    public ResponseEntity<ApiResponse> countProductsByBrandAndName(@RequestParam String brand, @RequestParam String name){
        try {
            long productCount = productService.countProductsByBrandAndName(brand, name);
            return ResponseEntity.ok(new ApiResponse("Product count!", productCount));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(e.getMessage(), null));
        }
    }
}
