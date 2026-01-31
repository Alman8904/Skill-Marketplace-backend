package com.Skill.Marketplace.SM.DTO.UserSkillDTO;
import com.Skill.Marketplace.SM.Entities.ServiceMode;
import lombok.Data;

@Data
public class updateUserSkillDTO {
    private String description;
    private double rate;
    private int experience;
    private ServiceMode serviceMode;
}
