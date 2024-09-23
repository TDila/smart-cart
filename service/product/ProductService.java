package com.vulcan.smartcart.service.product;

import com.vulcan.smartcart.dto.ImageDTO;
import com.vulcan.smartcart.dto.ProductDto;
import com.vulcan.smartcart.exceptions.AlreadyExistsException;
import com.vulcan.smartcart.exceptions.ResourceNotFoundException;
import com.vulcan.smartcart.model.Category;
import com.vulcan.smartcart.model.Image;
import com.vulcan.smartcart.model.Product;
import com.vulcan.smartcart.repository.CategoryRepository;
import com.vulcan.smartcart.repository.ImageRepository;
import com.vulcan.smartcart.repository.ProductRepository;
import com.vulcan.smartcart.request.AddProductRequest;
import com.vulcan.smartcart.request.UpdateProductRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{
    // if you're using @RequiredArgsConstructor always make sure to use 'final'
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final ModelMapper modelMapper;

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    @Override
    public Product addProduct(AddProductRequest request) {
        //check if the category is found  in the DB
        //if yes, set it as the new product category
        //if no, then save it as a new category
        //then set as the new product category.
        logger.info("Attempting to add product: {} - {}", request.getBrand(), request.getName());
        if(productExists(request.getName(), request.getBrand())){
            logger.warn("Product {} - {} already exists, throwing AlreadyExistsException", request.getBrand(), request.getName());
            throw new AlreadyExistsException(request.getBrand()+" "+request.getName()+" already exists, you may update this product instead.");
        }

        logger.info("Checking if category {} exists in the database", request.getCategory().getName());
        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory().getName()))
                .orElseGet(() -> {
                    logger.info("Category {} not found, creating a new one", request.getCategory().getName());
                    Category newCategory = new Category(request.getCategory().getName());
                    Category savedCategory = categoryRepository.save(newCategory);
                    logger.info("Category {} successfully created with ID {}", savedCategory.getName(), savedCategory.getId());
                    return savedCategory;
                });
        request.setCategory(category);

        Product newProduct = createProduct(request, category);
        Product savedProduct = productRepository.save(newProduct);
        logger.info("Product {} - {} successfully added with ID {}", savedProduct.getBrand(), savedProduct.getName(), savedProduct.getId());
        return savedProduct;
    }

    private boolean productExists(String name, String brand){
        return productRepository.existsByNameAndBrand(name, brand);
    }

    private Product createProduct(AddProductRequest request, Category category){
        return new Product(
                request.getName(),
                request.getBrand(),
                request.getPrice(),
                request.getInventory(),
                request.getDescription(),
                category
        );
    }

    @Override
    public Product getProductById(Long id) {
        logger.info("Fetching product with ID: {}",id);
        Optional<Product> product = productRepository.findById(id);
        if(product.isPresent()){
            logger.debug("Product found: {}", product.get());
            return product.get();
        } else {
            logger.warn("No product found with ID: {}",id);
            throw new ResourceNotFoundException("Product not found!");
        }
    }

    @Override
    public void deleteProductById(Long id) {
        productRepository.findById(id).ifPresentOrElse(productRepository::delete, () -> {
            logger.warn("No product found with ID: {}", id);
            throw new ResourceNotFoundException("Product not found!");
        });
        logger.info("Product successfully deleted with ID: {}", id);
    }

    @Override
    public Product updateProduct(UpdateProductRequest request, Long productId) {
        logger.info("Attempting to update product with ID: {}", productId);

        return productRepository.findById(productId)
                .map(existingProduct -> {
                    logger.info("Product with ID: {} found, updating product details", productId);
                    return updateExistingProduct(existingProduct, request);
                })
                .map(product -> {
                    Product savedProduct = productRepository.save(product);
                    logger.info("Product with ID: {} successfully updated", savedProduct.getId());
                    return savedProduct;
                })
                .orElseThrow(() -> {
                    logger.error("Product with ID: {} not found, throwing ResourceNotFoundException", productId);
                    return new ResourceNotFoundException("Product not found!");
                });
    }

    private Product updateExistingProduct(Product existingProduct, UpdateProductRequest request){
        existingProduct.setName(request.getName());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setInventory(request.getInventory());
        existingProduct.setDescription(request.getDescription());

        Category category = categoryRepository.findByName(request.getCategory().getName());

        existingProduct.setCategory(category);
        return existingProduct;
    }

    @Override
    public List<Product> getAllProducts() {
        logger.info("Fetching all products from the database");
        List<Product> products = productRepository.findAll();
        logger.info("Fetched {} products", products.size());
        return products;
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        logger.info("Fetching products for category: {}", category);
        List<Product> products = productRepository.findByCategoryName(category);
        logger.info("Fetched {} products for category: {}", products.size(), category);
        return products;
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        logger.info("Fetching products for brand: {}", brand);
        List<Product> products = productRepository.findByBrand(brand);
        logger.info("Fetched {} products for brand: {}", products.size(), brand);
        return products;
    }
    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        logger.info("Fetching products for category: {} and brand: {}", category, brand);
        List<Product> products = productRepository.findByCategoryNameAndBrand(category, brand);
        logger.info("Fetched {} products for category: {} and brand: {}", products.size(), category, brand);
        return products;
    }

    @Override
    public List<Product> getProductsByName(String name) {
        logger.info("Fetching products with name: {}", name);
        List<Product> products = productRepository.findByName(name);
        logger.info("Fetched {} products with name: {}", products.size(), name);
        return products;
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        logger.info("Fetching products with brand: {} and name: {}", brand, name);
        List<Product> products = productRepository.findByBrandAndName(brand, name);
        logger.info("Fetched {} products with brand: {} and name: {}", products.size(), brand, name);
        return products;
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        logger.info("Counting products with brand: {} and name: {}", brand, name);
        Long count = productRepository.countByBrandAndName(brand, name);
        logger.info("Found {} products with brand: {} and name: {}", count, brand, name);
        return count;
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products){
        logger.info("Converting {} products to DTOs", products.size());
        List<ProductDto> productDtos = products.stream().map(this::convertToDTO).toList();
        logger.info("Successfully converted {} products to DTOs", productDtos.size());
        return productDtos;
    }

    @Override
    public ProductDto convertToDTO(Product product){
        logger.info("Converting product with ID: {} to ProductDTO", product.getId());
        ProductDto productDTO = modelMapper.map(product, ProductDto.class);

        logger.info("Fetching images for product with ID: {}", product.getId());
        List<Image> images = imageRepository.findByProductId(product.getId());

        logger.info("Converting {} images to ImageDTOs", images.size());
        List<ImageDTO> imageDTOS = images.stream()
                .map(image -> modelMapper.map(image, ImageDTO.class))
                .toList();

        productDTO.setImages(imageDTOS);
        logger.info("Successfully converted product with ID: {} to ProductDTO", product.getId());

        return productDTO;
    }
}
