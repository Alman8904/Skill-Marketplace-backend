package com.Skill.Marketplace.SM.Controllers;
import com.Skill.Marketplace.SM.DTO.categoryDTO.CategoryResponseDTO;
import com.Skill.Marketplace.SM.DTO.skillDTO.CreateSkillDTO;
import com.Skill.Marketplace.SM.DTO.skillDTO.SkillResponseDTO;
import com.Skill.Marketplace.SM.DTO.skillDTO.UpdateSkillDTO;
import com.Skill.Marketplace.SM.Entities.Skill;
import com.Skill.Marketplace.SM.Services.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;



    @PostMapping
    public ResponseEntity<?> createSkill(@RequestBody CreateSkillDTO dto){
        Skill savedSkill = skillService.create(dto);
        return ResponseEntity.ok(
                new SkillResponseDTO(
                        savedSkill.getId(),
                        savedSkill.getSkillName(),
                        savedSkill.getCategory() != null ? new CategoryResponseDTO(
                                savedSkill.getCategory().getCategoryId(),
                                savedSkill.getCategory().getCategoryName()
                        ) : null
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSkill(@PathVariable Long id , @RequestBody UpdateSkillDTO dto){
        skillService.update(id, dto);
        return ResponseEntity.ok(
                new SkillResponseDTO(
                        id,
                        dto.getSkillName(),
                        null
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSkillById(@PathVariable Long id){
        Skill skill = skillService.getById(id);
        return ResponseEntity.ok(
                new SkillResponseDTO(
                        skill.getId(),
                        skill.getSkillName(),
                        skill.getCategory() != null ? new CategoryResponseDTO(
                                skill.getCategory().getCategoryId(),
                                skill.getCategory().getCategoryName()
                        ) : null
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllSkills(){
        List<Skill> categories = skillService.getAll();
        List<SkillResponseDTO> response =  categories.stream().map(
                skill -> new SkillResponseDTO(
                        skill.getId(),
                        skill.getSkillName(),
                        skill.getCategory() != null ? new CategoryResponseDTO(
                                skill.getCategory().getCategoryId(),
                                skill.getCategory().getCategoryName()
                        ) : null
                )
        ).toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSkillById(@PathVariable Long id){
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
