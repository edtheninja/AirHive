package com.airhive.backend.repository;

import com.airhive.backend.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {

    boolean existsByCrewCode(String crewCode);
}
