package com.localservicefinder.service;

import com.localservicefinder.entity.ServiceCategory;
import com.localservicefinder.repository.ServiceCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCategoryService {

    @Autowired
    private ServiceCategoryRepository categoryRepository;

    public List<ServiceCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public ServiceCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public ServiceCategory saveCategory(ServiceCategory category) {
        if (category.getId() == null && categoryRepository.findByNameIgnoreCase(category.getName()).isPresent()) {
            throw new RuntimeException("Category with this name already exists");
        }
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
