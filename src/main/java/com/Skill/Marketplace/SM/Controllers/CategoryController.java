package com.Skill.Marketplace.SM.Controllers;
import com.Skill.Marketplace.SM.DTO.categoryDTO.CategoryResponseDTO;
import com.Skill.Marketplace.SM.DTO.categoryDTO.CreateCategoryDTO;
import com.Skill.Marketplace.SM.DTO.categoryDTO.UpdateCategoryDTO;
import com.Skill.Marketplace.SM.Entities.Category;
import com.Skill.Marketplace.SM.Services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;



    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CreateCategoryDTO request){

        Category savedCategory = categoryService.create(request);

        return ResponseEntity.ok(
                new CategoryResponseDTO(
                        savedCategory.getCategoryId(),
                        savedCategory.getCategoryName()
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id , @RequestBody UpdateCategoryDTO dto){

        categoryService.update(id, dto);

        return ResponseEntity.ok(
                new CategoryResponseDTO(
                        id,
                        dto.getCategoryName()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id){
            Category category = categoryService.getById(id);
        return ResponseEntity.ok(
                new CategoryResponseDTO(
                        category.getCategoryId(),
                        category.getCategoryName()
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories(){
        List<Category> categories = categoryService.getAll();
        List<CategoryResponseDTO> response  = categories.stream().map(
                category -> new CategoryResponseDTO(
                        category.getCategoryId(),
                        category.getCategoryName()
                )
        ).toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategoryById(@PathVariable Long id){
         categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
