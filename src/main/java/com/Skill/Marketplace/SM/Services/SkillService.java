package com.Skill.Marketplace.SM.Services;
import com.Skill.Marketplace.SM.DTO.skillDTO.CreateSkillDTO;
import com.Skill.Marketplace.SM.DTO.skillDTO.UpdateSkillDTO;
import com.Skill.Marketplace.SM.Entities.Category;
import com.Skill.Marketplace.SM.Entities.Skill;
import com.Skill.Marketplace.SM.Repo.CategoryRepo;
import com.Skill.Marketplace.SM.Repo.SkillsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {

    @Autowired
    private SkillsRepo skillsRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    public Skill create(CreateSkillDTO dto){

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(()-> new RuntimeException("No Category found"));

        Skill skill = new Skill();
        skill.setSkillName(dto.getSkillName());

        skill.setCategory(category);

        return skillsRepo.save(skill);
    }

    public Skill update(Long id , UpdateSkillDTO dto){
        Skill skill = skillsRepo.findById(id)
                .orElseThrow(()-> new RuntimeException("No skills found"));

        skill.setSkillName(dto.getSkillName());


        Category category =  categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(()-> new RuntimeException("No category found"));
        skill.setCategory(category);

        return skillsRepo.save(skill);

    }

    public Skill getById(Long id){
        return skillsRepo.findById(id)
                .orElseThrow(()->new RuntimeException("No skills found"));
    }

    public List<Skill> getAll(){
        return skillsRepo.findAll();
    }

    public void delete(Long id){
        skillsRepo.deleteById(id);
    }
}
