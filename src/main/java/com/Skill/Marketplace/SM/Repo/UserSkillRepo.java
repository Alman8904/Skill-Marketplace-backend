package com.Skill.Marketplace.SM.Repo;
import com.Skill.Marketplace.SM.Entities.UserModel;
import com.Skill.Marketplace.SM.Entities.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSkillRepo extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUser(UserModel user);
}
