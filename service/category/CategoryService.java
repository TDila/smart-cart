package com.vulcan.smartcart.service.category;

import com.vulcan.smartcart.exceptions.AlreadyExistsException;
import com.vulcan.smartcart.exceptions.ResourceNotFoundException;
import com.vulcan.smartcart.model.Category;
import com.vulcan.smartcart.repository.CategoryRepository;
import com.vulcan.smartcart.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService{
    private final CategoryRepository categoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    @Override
    public Category getCategoryById(Long id) {
        logger.info("Fetching category with ID: {}", id);
        return categoryRepository.findById(id).orElseThrow(() -> {
            logger.error("Category with ID: {} not found", id);
            return new ResourceNotFoundException("Category not found!");
        });
    }

    @Override
    public Category getCategoryByName(String name) {
        logger.info("Fetching category with name: {}", name);
        return categoryRepository.findByName(name);
    }

    @Override
    public List<Category> getAllCategories() {
        logger.info("Fetching all categories");
        return categoryRepository.findAll();
    }

    @Override
    public Category addCategory(Category category) {
        logger.info("Adding new category: {}", category.getName());
        return Optional.of(category)
                .filter(c -> !categoryRepository.existsByName(c.getName()))
                .map(categoryRepository::save)
                .orElseThrow(() -> {
                    logger.error("Category with name: {} already exists", category.getName());
                    return new AlreadyExistsException(category.getName() + " already exists!");
                });
    }

    @Override
    public Category updateCategory(Category category, Long id) {
        logger.info("Updating category with ID: {}", id);
        return Optional.ofNullable(getCategoryById(id)).map(oldCategory -> {
            oldCategory.setName(category.getName());
            Category updatedCategory = categoryRepository.save(oldCategory);
            logger.info("Category with ID: {} updated successfully", id);
            return updatedCategory;
        }).orElseThrow(() -> {
            logger.error("Category with ID: {} not found for update", id);
            return new ResourceNotFoundException("Category not found!");
        });
    }

    @Override
    public void deleteCategoryById(Long id) {
        logger.info("Deleting category with ID: {}", id);
        categoryRepository.findById(id).ifPresentOrElse(category -> {
            categoryRepository.delete(category);
            logger.info("Category with ID: {} deleted successfully", id);
        }, () -> {
            logger.error("Category with ID: {} not found for deletion", id);
            throw new ResourceNotFoundException("Category not found!");
        });
    }

}
