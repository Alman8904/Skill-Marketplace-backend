package com.Skill.Marketplace.SM.DTO.skillDTO;
import com.Skill.Marketplace.SM.DTO.categoryDTO.CategoryResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SkillResponseDTO {
    private Long id;
    private String skillName;
    private CategoryResponseDTO category;
}
