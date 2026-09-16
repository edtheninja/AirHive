package com.airhive.backend.config;

import com.airhive.backend.entity.CrewMember;
import com.airhive.backend.repository.CrewMemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevCrewSeeder {

    @Bean
    CommandLineRunner seedCrewMembers(
            CrewMemberRepository crewMemberRepository
    ) {
        return args -> {
            seed(
                    crewMemberRepository,
                    "C1",
                    "Capt. Arjun Mehta",
                    "Captain",
                    "DEL",
                    "On Duty",
                    11,
                    "AI302",
                    "Valid — Mar 2027",
                    "AM"
            );

            seed(
                    crewMemberRepository,
                    "C2",
                    "Capt. Lena Fischer",
                    "Captain",
                    "FRA",
                    "Resting",
                    4,
                    "AI914",
                    "Valid — Nov 2026",
                    "LF"
            );

            seed(
                    crewMemberRepository,
                    "C3",
                    "F/O Rahul Nair",
                    "First Officer",
                    "BOM",
                    "Available",
                    22,
                    "AI458",
                    "Valid — Jun 2027",
                    "RN"
            );

            seed(
                    crewMemberRepository,
                    "C4",
                    "Sofia Alvarez",
                    "Purser",
                    "DXB",
                    "On Duty",
                    9,
                    "AI302",
                    "Valid — Jan 2027",
                    "SA"
            );

            seed(
                    crewMemberRepository,
                    "C5",
                    "Chen Wei",
                    "Cabin Crew",
                    "SIN",
                    "Available",
                    18,
                    "AI770",
                    "Renew — Sep 2026",
                    "CW"
            );

            seed(
                    crewMemberRepository,
                    "C6",
                    "Priya Sharma",
                    "Cabin Crew",
                    "DEL",
                    "Leave",
                    48,
                    "—",
                    "Valid — Aug 2027",
                    "PS"
            );

            seed(
                    crewMemberRepository,
                    "C7",
                    "Tomas Novak",
                    "Engineer",
                    "LHR",
                    "On Duty",
                    7,
                    "Ground",
                    "Valid — Feb 2027",
                    "TN"
            );

            seed(
                    crewMemberRepository,
                    "C8",
                    "Aisha Rahman",
                    "First Officer",
                    "DOH",
                    "Available",
                    26,
                    "AI221",
                    "Valid — Dec 2026",
                    "AR"
            );
        };
    }

    private void seed(
            CrewMemberRepository repository,
            String crewCode,
            String name,
            String role,
            String base,
            String availability,
            int restHours,
            String nextFlight,
            String medical,
            String initials
    ) {
        if (repository.existsByCrewCode(crewCode)) {
            return;
        }

        CrewMember crewMember = new CrewMember();

        crewMember.setCrewCode(crewCode);
        crewMember.setName(name);
        crewMember.setRole(role);
        crewMember.setBase(base);
        crewMember.setAvailability(availability);
        crewMember.setRestHours(restHours);
        crewMember.setNextFlight(nextFlight);
        crewMember.setMedical(medical);
        crewMember.setInitials(initials);

        repository.save(crewMember);
    }
}
