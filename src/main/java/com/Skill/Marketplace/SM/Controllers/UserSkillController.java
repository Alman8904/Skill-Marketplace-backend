package com.Skill.Marketplace.SM.Controllers;
import com.Skill.Marketplace.SM.DTO.UserSkillDTO.updateUserSkillDTO;
import com.Skill.Marketplace.SM.DTO.UserSkillDTO.AssignSkillDTO;
import com.Skill.Marketplace.SM.Services.UserSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-skills")
public class UserSkillController {

    @Autowired
    private UserSkillService userSkillService;

    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping("/assign")
    public ResponseEntity<?> assignSkillToUser( @RequestBody AssignSkillDTO dto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        userSkillService.assignSkills(username ,dto);
        return ResponseEntity.ok("Skill(s) assigned to user");
    }

    @PreAuthorize("hasRole('PROVIDER')" )
    @GetMapping("/all-userSkills")
    public ResponseEntity<?> getSkillsByUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.ok(userSkillService.getSkillsByUser(username));
    }

    @PreAuthorize("hasRole('PROVIDER')" )
    @PutMapping("/update/{userSkillId}")
    public ResponseEntity<?> updateUserSkill(@PathVariable Long userSkillId, @RequestBody updateUserSkillDTO dto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        userSkillService.updateUserSkill(userSkillId, username , dto);
        return ResponseEntity.ok("UserSkill updated successfully");
    }

    @PreAuthorize("hasRole('PROVIDER')" )
    @DeleteMapping("/deactivate/{userSkillId}")
    public ResponseEntity<?> deactivateUserSkill(@PathVariable Long userSkillId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        userSkillService.deactivateUserSkill(userSkillId, username);
        return ResponseEntity.ok("UserSkill deactivated successfully");
    }


}
