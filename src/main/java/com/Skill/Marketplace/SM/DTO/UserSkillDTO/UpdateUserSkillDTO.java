package com.Skill.Marketplace.SM.DTO.UserSkillDTO;

import com.Skill.Marketplace.SM.Entities.ServiceMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserSkillDTO {

    @NotBlank
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull
    @PositiveOrZero(message = "Rate must be a positive number")
    private double rate;

    @NotNull
    @PositiveOrZero(message = "Experience must be a positive number")
    private int experience;

    @NotNull(message = "Service mode is required")
    private ServiceMode serviceMode;
}
