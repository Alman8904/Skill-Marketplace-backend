package com.Skill.Marketplace.SM.Repo;
import com.Skill.Marketplace.SM.Entities.Skill;
import com.Skill.Marketplace.SM.Entities.UserModel;
import com.Skill.Marketplace.SM.Entities.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepo extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUser(UserModel user);

    Optional<UserSkill> findByUserAndSkill(UserModel user, Skill skill);

    @Query("""
    SELECT us FROM UserSkill us
    WHERE LOWER(us.skill.skillName) LIKE LOWER(CONCAT('%', :skillName, '%'))
    AND us.isActive = true
""")
    List<UserSkill> searchBySkillName(@Param("skillName") String skillName);



}
