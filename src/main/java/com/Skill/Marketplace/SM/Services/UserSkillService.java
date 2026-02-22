package com.Skill.Marketplace.SM.Services;

import com.Skill.Marketplace.SM.DTO.UserSkillDTO.AssignSkillDTO;
import com.Skill.Marketplace.SM.DTO.UserSkillDTO.UpdateUserSkillDTO;
import com.Skill.Marketplace.SM.DTO.UserSkillDTO.UserSkillResponseDTO;
import com.Skill.Marketplace.SM.Entities.*;
import com.Skill.Marketplace.SM.Exception.ConflictException;
import com.Skill.Marketplace.SM.Exception.ForbiddenException;
import com.Skill.Marketplace.SM.Exception.ResourceNotFoundException;
import com.Skill.Marketplace.SM.Repo.SkillsRepo;
import com.Skill.Marketplace.SM.Repo.UserRepo;
import com.Skill.Marketplace.SM.Repo.UserSkillRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserSkillService {

    @Autowired
    private UserSkillRepo userSkillRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SkillsRepo skillsRepo;

    @Transactional
    public void assignSkills(String username, AssignSkillDTO dto) {

        UserModel user = userRepo.getUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getUserType() == UserType.CONSUMER) {
            throw new ForbiddenException("Consumers cannot offer skills");
        }

        for (AssignSkillDTO.SkillData skillData : dto.getSkills()) {

            Skill skill = skillsRepo.findById(skillData.getSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

            Optional<UserSkill> existingSkill = userSkillRepo
                    .findByUserAndSkillAndIsActiveTrue(user, skill);

            if (existingSkill.isPresent()) {
                throw new ConflictException(
                        "User already has an active assignment for skill: " + skill.getSkillName()
                );
            }

            UserSkill userSkill = new UserSkill();
            userSkill.setUser(user);
            userSkill.setSkill(skill);
            userSkill.setDescription(skillData.getDescription());
            userSkill.setRate(skillData.getRate());
            userSkill.setExperience(skillData.getExperience());
            userSkill.setServiceMode(skillData.getServiceMode());
            userSkill.setActive(true);

            userSkillRepo.save(userSkill);
        }
    }

    @Transactional
    public void deactivateUserSkill(Long userSkillId, String username) {
        UserSkill userSkill = userSkillRepo.findById(userSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSkill not found"));

        if (!userSkill.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Unauthorized to deactivate this skill");
        }

        userSkill.setActive(false);
        userSkillRepo.save(userSkill);
    }

    @Transactional
    public void updateUserSkill(Long userSkillId, String username, UpdateUserSkillDTO skillData) {
        UserSkill userSkill = userSkillRepo.findById(userSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSkill not found"));

        if (!userSkill.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Unauthorized to update this skill");
        }

        userSkill.setDescription(skillData.getDescription());
        userSkill.setRate(skillData.getRate());
        userSkill.setExperience(skillData.getExperience());
        userSkill.setServiceMode(skillData.getServiceMode());

        userSkillRepo.save(userSkill);
    }

    public List<UserSkillResponseDTO> getSkillsByUser(String username) {
        UserModel user = userRepo.getUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userSkillRepo.findByUser(user)
                .stream()
                .filter(us -> us.isActive())
                .map(us -> new UserSkillResponseDTO(
                        us.getUserSkillId(),
                        us.getSkill().getSkillName(),
                        us.getDescription(),
                        us.getRate(),
                        us.getExperience(),
                        us.getServiceMode()
                ))
                .toList();

    }

    public Page<UserSkill> searchProvidersBySkill(
            String skill, Double minRate, Double maxRate,
            ServiceMode serviceMode, Integer minExperience, Pageable pageable) {

        return userSkillRepo.searchBySkill(skill, minRate, maxRate, serviceMode, minExperience, pageable);
    }
}
